package main.java.com.own.plugin.bpep.generator;

import static main.java.com.own.plugin.bpep.resolver.Resolver.getName;
import static main.java.com.own.plugin.bpep.resolver.Resolver.getType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.text.edits.MalformedTreeException;

public class BuilderGenerator implements Generator {

	@Override
	public void generate(ICompilationUnit cu, List<IField> fields) {

		try {

			IType clazz = cu.getTypes()[0];
			String builderClassName = clazz.getElementName() + "Builder";

			removeOldClassConstructor(cu);
			removeOldBuilderClass(cu);

			IBuffer buffer = cu.getBuffer();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println();
			pw.println("\tpublic static " + builderClassName + " " + decapitalize(builderClassName) + "() {");
			pw.println("\t\treturn new " + builderClassName + "();");
			pw.println("\t}");
			pw.println("");

			int pos = clazz.getSourceRange().getOffset() + clazz.getSourceRange().getLength() - 1;
			pw.println("\tpublic static class " + builderClassName + " {");
			createFieldDeclarations(pw, fields);
			pw.println("\t\tprivate " + builderClassName + "() {");
			pw.println("\t\t\tsuper();");
			pw.println("\t\t}");

			createBuilderMethods(pw, fields, builderClassName);
			createBuilderConstructor(pw, clazz, fields);
			pw.println("\t}");
			buffer.replace(pos, 0, sw.toString());

		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		}
	}

	private void removeOldBuilderClass(ICompilationUnit cu) throws JavaModelException {
		for (IType type : cu.getTypes()[0].getTypes()) {
			if (type.getElementName().equals("Builder") && type.isClass()) {
				type.delete(true, null);
				break;
			}
		}
	}

	private void removeOldClassConstructor(ICompilationUnit cu) throws JavaModelException {
		for (IMethod method : cu.getTypes()[0].getMethods()) {
			if (method.isConstructor() && method.getParameterTypes().length == 1
					&& method.getParameterTypes()[0].equals("QBuilder;")) {
				method.delete(true, null);
				break;
			}
		}
	}

	private void createBuilderConstructor(PrintWriter pw, IType clazz, List<IField> fields) {
		String clazzName = clazz.getElementName();
		String clazzVariable = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
		pw.println("\t\tpublic " + clazzName + " build(){");
		pw.println("\t\t\t" + clazzName + " " + clazzVariable + "= new " + clazzName + "();");
		for (IField field : fields) {
			String name = getName(field);
			pw.println("\t\t\t" + clazzVariable + "." + name + "= this." + name + ";");
		}
		pw.println("\t\t\treturn " + clazzVariable + ";\n\t\t}");
	}

	private void createBuilderMethods(PrintWriter pw, List<IField> fields, String builderClassname) throws JavaModelException {
		for (IField field : fields) {
			String fieldName = getName(field);
			String fieldType = getType(field);
			String baseName = getFieldBaseName(fieldName);
			String parameterName = baseName;
			pw.println("\t\tpublic " + builderClassname + " set" + capitalize(baseName) + "(" + fieldType + " " + parameterName + ") {");
			pw.println("\t\t\tthis." + fieldName + "=" + parameterName + ";");
			pw.println("\t\t\treturn this;\n\t\t}");
		}
	}

	private String getFieldBaseName(String fieldName) {
		IJavaProject javaProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject());
		return NamingConventions.getBaseName(NamingConventions.VK_INSTANCE_FIELD, fieldName, javaProject);
	}

	private void createFieldDeclarations(PrintWriter pw, List<IField> fields) throws JavaModelException {
		for (IField field : fields) {
			pw.println("\t\tprivate " + getType(field) + " " + getName(field) + ";");
		}
	}

	private String decapitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {

			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
	
	private String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
}
