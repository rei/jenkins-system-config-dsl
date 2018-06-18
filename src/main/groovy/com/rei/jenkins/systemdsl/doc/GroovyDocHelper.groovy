package com.rei.jenkins.systemdsl.doc

import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef
import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.groovydoc.GroovyMethodDoc
import org.codehaus.groovy.groovydoc.GroovyProgramElementDoc
import org.codehaus.groovy.groovydoc.GroovyRootDoc
import org.codehaus.groovy.tools.groovydoc.ArrayClassDocWrapper
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool

class GroovyDocHelper {

    final GroovyRootDoc rootDoc

    GroovyDocHelper(Path sourcePath) {
        rootDoc = createRootDoc(sourcePath)
    }

    private static GroovyRootDoc createRootDoc(Path sourcePath) {
        List filePaths = []
        sourcePath.eachFileRecurse { Path file ->
            if (Files.isRegularFile(file)) {
                filePaths.add(sourcePath.relativize(file).toString())
            }
        }
        GroovyDocTool tool = new GroovyDocTool([sourcePath.toString()] as String[])
        tool.add filePaths

        return tool.rootDoc
    }

    GroovyClassDoc getGroovyClassDoc(Class clazz) {
        String name = '/' + clazz.name.replaceAll('\\.', '/').replace('$', '.')
        return rootDoc.classes().find { it.fullPathName == name || it.fullPathName == name[1..-1] }
    }

    static boolean hasAnnotation(GroovyProgramElementDoc doc, Class annotationClass) {
        GroovyAnnotationRef[] annotations = doc.annotations()
        return annotations.any { it.name() == annotationClass.name.replaceAll('\\.', '/') }
    }

    static Method getMethodFromGroovyMethodDoc(GroovyMethodDoc methodDoc, Class clazz) {
        Method method = clazz.methods.findAll { it.name == methodDoc.name() }.find { Method method ->
            List docParamNames = methodDoc.parameters().collect {
                String name = it.type()?.qualifiedTypeName() ?: it.typeName()
                if (name.startsWith('.')) {
                    name = name[1..-1]
                }

                if (it.type() && it.type() instanceof ArrayClassDocWrapper) {
                    return "[L$name;"
                }

                Map primitiveToArrayName = [
                    'byte': '[B',
                    'short': '[S',
                    'int': '[I',
                    'long': '[J',
                    'float': '[F',
                    'double': '[D',
                    'char': '[C',
                    'boolean': '[Z',
                ]

                if (it.vararg()) {
                    if (primitiveToArrayName[name]) {
                        return primitiveToArrayName[name]
                    }
                    return "[L$name;"
                } else if (name == 'def') {
                    return 'java.lang.Object'
                } else {
                    return name
                }
            }
            docParamNames == method.parameterTypes*.name ||
                docParamNames == method.parameterTypes*.canonicalName ||
                docParamNames == method.parameterTypes.collect { it.enum ? it.simpleName : it.name }
        }

        return method
    }

    GroovyMethodDoc[] getAllMethods(Class clazz) {
        GroovyClassDoc classDoc = getGroovyClassDoc(clazz)
        List<GroovyMethodDoc> methodDocs = classDoc?.methods() ?: []
        Class superclass = clazz.superclass
        if (superclass) {
            addSuperclassMethods superclass, methodDocs
        }

        if (clazz.interface) {
            clazz.interfaces.each { addSuperclassMethods it, methodDocs }
        }
        return methodDocs
    }

    private void addSuperclassMethods(Class superclass, List<GroovyMethodDoc> methodDocs) {
        getAllMethods(superclass).each { GroovyMethodDoc superclassMethod ->
            boolean overridden = methodDocs.find {
                it.name() == superclassMethod.name() &&
                    it.parameters()*.typeName() == superclassMethod.parameters()*.typeName()
            }
            if (!overridden) {
                methodDocs << superclassMethod
            }
        }
    }
}
