package com.rei.jenkins.systemdsl.doc

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.codehaus.groovy.groovydoc.GroovyMethodDoc
import org.codehaus.groovy.groovydoc.GroovyParameter
import org.codehaus.groovy.tools.groovydoc.ArrayClassDocWrapper

import com.rei.jenkins.systemdsl.JenkinsSystemConfigDsl

class ApiDocGenerator {

    final private GroovyDocHelper docHelper = new GroovyDocHelper(groovySourceDirectory)
    final private Class rootClass = JenkinsSystemConfigDsl
    List<SystemDslMethod> roots
    Map<Method, SystemDslMethod> processedMethods = [:]

    /**
     *    arg0 is the format, either html or txt
     /*   arg1 is a file -- if null, print to console
     */
    static void main(String[] args) {
        boolean isHtml
        def outFile
        println args
        if(args.length >= 1 && (args[0] == '-txt' || args[0] == '-html')) {
            isHtml = args[0] == '-html'
        } else {
            println("Please specify file format -- either -txt or -html. Optionally add an output file Exiting.")
            System.exit(1)
        }

        if(args.length == 2) {
            outFile = new File(args[1])
            new File(outFile.getParent()).mkdirs() //create parent folder if needed
        }

        def generator = new ApiDocGenerator()
        generator.generateApi()

        String outText
        if(isHtml) {
            String template = getClass().getResourceAsStream('/doc-template.html').text
            StringBuilder sb = new StringBuilder()
            generator.roots.each { sb.append(it.toHtml()) }
            outText = template.replace('<!--GROOVY METHODS-->', sb.toString())
        } else {
            StringBuilder sb = new StringBuilder()
            generator.roots.each {sb.append(it.toString())}
            outText = sb.toString()
        }

        if(outFile == null) {
            println(outText)
        } else {
            println("Writing generated docs to ${outFile}")
            outFile.text = outText
        }
    }

    void generateApi() {
        roots = processClass(rootClass, null)
    }

    private List<SystemDslMethod> processClass(Class clazz, SystemDslMethod parent) {
        return getMethodsForClass(clazz, parent)
    }


    private List<SystemDslMethod> getMethodsForClass(Class clazz, SystemDslMethod parent) {
        return clazz.methods.findAll {
            !it.name.startsWith('get') &&
                !it.name.startsWith('set') &&
                !it.name.startsWith('is') &&
                !(it.declaringClass in [Object, Script]) &&
                Modifier.isPublic(it.modifiers) &&
                !Modifier.isStatic(it.modifiers) &&
                !it.name.contains('$') &&
                !it.isAnnotationPresent(Helper) &&
                !(it.name in ['invokeMethod', 'methodMissing', 'propertyMissing', 'save'])
        }.sort { it.name }
         .collect { processMethod(it, clazz, parent) }
    }

    private SystemDslMethod processMethod(Method method, Class clazz, SystemDslMethod parent) {
        if (processedMethods[method]) {
            //println "returning already processed $method"
            return processedMethods[method]
        }

        def dslMethod = new SystemDslMethod(method: method, parent: parent, declaredType: clazz)
        GroovyMethodDoc methodDoc = docHelper.getAllMethods(clazz)
                .findAll { it.name() == method.name }
                .find {  GroovyDocHelper.getMethodFromGroovyMethodDoc(it, clazz) == method }

        dslMethod.delegate = method.parameters.find { it.isAnnotationPresent(DelegatesTo) }?.getAnnotation(DelegatesTo)?.value()
        dslMethod.helpText = stripTags(removeParams(methodDoc?.commentText()))
        dslMethod.plugin = method.getAnnotation(RequiresPlugin)?.value()

        method.parameters.eachWithIndex { Parameter p, int i ->
            def docParam = methodDoc ? methodDoc?.parameters()[i] : null
            dslMethod.parameters << processParameter(p, docParam, method.getGenericParameterTypes()[i])
        }

        if (method.isAnnotationPresent(ExampleArgs)) {
            dslMethod.exampleArgs = method.getAnnotation(ExampleArgs).value() as List
        } else {
            dslMethod.exampleArgs = method.getParameters().collect { getDefaultExampleArg(it) }
        }

        if (method.isAnnotationPresent(JenkinsHelpTexts)) {
            method.getAnnotation(JenkinsHelpTexts).value().each {
                dslMethod.argHelpTexts << collapseLines(stripTags(getClass().classLoader.getResourceAsStream(it)?.text))
            }
        }

        if (dslMethod.delegate) {
            if (dslMethod.parameters.removeIf { it.type == Closure }) {
                (dslMethod.exampleArgs.size() - dslMethod.parameters.size()).times {
                    dslMethod.exampleArgs.remove(dslMethod.exampleArgs.size()-1)
                }
            }

            dslMethod.children = processClass(dslMethod.delegate, dslMethod)
        }

        if (!dslMethod.helpText && dslMethod.argHelpTexts.size() == 1 && dslMethod.parameters.empty) {
            dslMethod.helpText = dslMethod.argHelpTexts[0]
            dslMethod.argHelpTexts.clear()
        }

        processedMethods[method] = dslMethod
        return dslMethod
    }

    private DslMethodParam processParameter(Parameter parameter, GroovyParameter docParam, Type type) {
        def dslParam = new DslMethodParam(name: docParam?.name() ?: parameter.name, type: parameter.type)

        Class clazz
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (type as ParameterizedType)
            clazz = parameterizedType.rawType as Class
            dslParam.typeName = getSimpleClassName(clazz) + '<' + parameterizedType.actualTypeArguments.collect {
                getSimpleClassName it
            }.join(', ') + '>'
        } else {
            clazz = type as Class
            if (docParam?.vararg()) {
                dslParam.typeName = getSimpleClassName(clazz.componentType) + '...'
            } else if (docParam?.type() && docParam?.type() instanceof ArrayClassDocWrapper) {
                dslParam.typeName = getSimpleClassName(clazz.componentType) + '[]'
            } else {
                dslParam.typeName = getSimpleClassName(clazz)
            }
            dslParam.typeName = dslParam.typeName.replaceAll('\\$', '.') // fix inner class names
        }

        dslParam.validValues = getValidValues(parameter)

        return dslParam
    }

    private static String getDefaultExampleArg(Parameter parameter) {
        def type = parameter.type
        if (type == Integer.TYPE || type == Integer.class  || type == Long.TYPE || type == Long.class) {
            return new Random().nextInt(9)+1 as String
        }

        if (parameter.isAnnotationPresent(ValidValues)) {
            return getValidValues(parameter)[0]
        }

        if (type.isEnum()) {
            return type.getSimpleName() + '.' + type.getEnumConstants()[0] as String
        }

        if (type == Boolean.TYPE || type == Boolean.class) {
            return new Random().nextBoolean() as String
        }

        if (CharSequence.isAssignableFrom(type)) {
            def methodName = parameter.getDeclaringExecutable().name.toLowerCase()
            if (methodName.contains('url')) {
                return "'http://localhost/'"
            }
            if (methodName.contains('server')) {
                return "'somehost.example.com'"
            }
            return "'some value'"
        }

        return 'null'
    }

    private static List<String> getValidValues(Parameter parameter) {
        if (parameter.type.isEnum()) {
            return parameter.type.enumConstants*.toString()
        }

        if (parameter.isAnnotationPresent(ValidValues)) {
            def valuesAnnotation = parameter.getAnnotation(ValidValues)
            if (valuesAnnotation.enumConstantsOf() != Enum) {
                return valuesAnnotation.enumConstantsOf()?.enumConstants*.toString()?.collect {
                    valuesAnnotation.enumAsString() ? "'${it}'" : it
                }
            }
            return valuesAnnotation.values() as List
        }

        return null
    }

    private static String getSimpleClassName(Class clazz) {
        String name = clazz.name
        List prefixes = [
            'java.lang.',
            'java.util.',
            'groovy.lang.',
        ]
        for (String prefix in prefixes) {
            if (name.startsWith(prefix)) {
                name = name[prefix.length()..-1]
                break
            }
        }
        return name
    }

    private static String stripTags(String text) {
        text?.replaceAll('<[^>]*>', '')?.trim()
    }

    private static String removeParams(String text) {
        text?.replaceAll('<DL><DT><B>Parameters:.+</DL>', '')
    }

    private static String collapseLines(String text) {
        text?.replace('\n', ' ')
    }

    private static Path getGroovySourceDirectory() {
        def path = Paths.get(ApiDocGenerator.class.protectionDomain.codeSource.location.toURI())
        while (path.nameCount > 0) {
            def groovyDir = path.resolve('src/main/groovy')
            if (Files.exists(groovyDir) && Files.isDirectory(groovyDir)) {
                return groovyDir
            }
            path = path.parent
        }
    }

    private static class SystemDslMethod {
        SystemDslMethod parent
        Method method
        List<SystemDslMethod> children = []
        Class delegate
        Class declaredType
        String helpText
        String plugin
        List<DslMethodParam> parameters = []
        List<String> exampleArgs = []
        List<String> argHelpTexts = []

        String toString() {
            return toString(0)
        }

        String toString(int level) {
            StringBuilder sb = new StringBuilder()

            def indentation = indent(level)

            if (plugin) {
                sb.append(indentation).append('// Requires Plugin: ').append(plugin).append('\n')
            }

            if (helpText) {
                sb.append(indentation).append('// ')
                  .append(helpText.replace(System.lineSeparator(), System.lineSeparator() + indentation + '// ')).append('\n')
                sb.append(indentation).append('// \n')
            }

            if (!parameters.empty) {
                parameters.eachWithIndex { p, i ->
                    if (argHelpTexts.size() > i) {
                        sb.append(indentation).append('// ')
                        if (parameters.size() > 1) {
                            sb.append(p.name).append(' - ')
                        }
                        sb.append(argHelpTexts[i]).append('\n')
                    }
                    if (p.validValues) {
                        sb.append(indentation).append('// ').append('Valid Values: ').append(p.validValues).append('\n')
                    }
                }

                if (!exampleArgs.empty) {
                    sb.append(indentation).append('// Example: ')
                            .append("${method.name}(${exampleArgs.join(', ')})${delegate ? ' { ... }' : ''}\n")
                }
            }
            sb.append(indentation).append(method.getName())

            if (children.empty || !parameters.empty) {
                sb.append('(')
                parameters.eachWithIndex { p, i ->
                     sb.append(p.typeName).append(' ').append(p.name)
                     if (i < parameters.size()-1) {
                         sb.append(', ')
                     }
                 }
                sb.append(')')
            }

            if (delegate && !children.empty) {
                sb.append(' {\n')
                children.each {
                    sb.append('\n').append(it.toString(level+1)).append('\n')
                }
                sb.append('\n').append(indentation).append('}\n')
            }

            return sb.toString()
        }

        String toHtml() {
            return toHtml(0)
        }

        String toHtml(int level) {
            StringBuilder sb = new StringBuilder()

            def indentation = indent(level)

            sb.append(indentation).append('<div class="method" >')
            sb.append('<a class="signature" onclick="javascript: this.classList.toggle(\'hide\');"><span class="arrow"> </span>')
                    .append(method.getName())

            if (children.empty || !parameters.empty) {
                sb.append('(')
                parameters.eachWithIndex { p, i ->
                    sb.append(p.typeName).append(' ').append(p.name)
                    if (i < parameters.size()-1) {
                        sb.append(', ')
                    }
                }
                sb.append(')')
            }
            sb.append('</a>')

            if (plugin) {
                sb.append(indentation).append('<div class="plugin"> Requires Plugin: ').append(plugin).append('</div>\n')
            }

            if (helpText) {
                sb.append(indentation).append('<div class="helptext"> \n').append(indentation).append(indent(1))
                        .append(helpText.replace(System.lineSeparator(), System.lineSeparator() + indentation)).append('\n')
                sb.append(indentation).append('</div> \n')
            }

            if (!parameters.empty) {
                sb.append(indentation).append('<ul class="params">\n')

                if (!exampleArgs.empty) {
                    sb.append(indentation).append('<li class="example"> Example: <pre>')
                            .append("${method.name}(${exampleArgs.join(', ')})${delegate ? ' { ... }' : ''}</pre></li>\n")
                }

                parameters.eachWithIndex { p, i ->
                    if (argHelpTexts.size() > i) {
                        sb.append(indentation).append('<li class="arg-help">')
                        sb.append(argHelpTexts[i]).append('</li>\n')
                    }
                    if (p.validValues) {
                        sb.append(indentation).append('<ul class="valid-values"> Valid Values: ')
                        p.validValues.each {sb.append('<li>').append(it).append('</li>')}
                        sb.append('</ul>\n')
                    }
                }

                sb.append(indentation).append('</ul>\n')
            }

            if (delegate && !children.empty) {
                children.each {
                    sb.append('\n').append(it.toHtml(level+1)).append('\n')
                }
                sb.append('\n').append(indentation).append('</div>\n')
            } else {
                sb.append('</div>\n')
            }

            return sb.toString()
        }

        private static String indent(int levels) {
            return ' ' * (4 * levels)
        }
    }

    private static class DslMethodParam {
        String name
        Class type
        String typeName
        List<String> validValues
    }
}
