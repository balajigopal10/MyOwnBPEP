package main.java.com.own.plugin.bpep.generator;

import static main.java.com.own.plugin.bpep.resolver.Resolver.getName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.text.edits.MalformedTreeException;

public class ToStringGenerator implements Generator {

	@Override
	public void generate(ICompilationUnit cu, List<IField> fields) {

		try {
			IType clazz = cu.getTypes()[0];
			IBuffer buffer = cu.getBuffer();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			int pos = clazz.getSourceRange().getOffset() + clazz.getSourceRange().getLength() - 1;

			pw.println("\tpublic String toString() {");
			pw.println("\t\treturn new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)");
			createFieldDeclarations(pw, fields);
			pw.println("\t\t\t\t.toString();");
			pw.println("\t}");
			buffer.replace(pos, 0, sw.toString());
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		}
	}

	private void createFieldDeclarations(PrintWriter pw, List<IField> fields) throws JavaModelException {
		for (IField field : fields) {
			pw.println("\t\t\t\t.append(\"" + getName(field) + "\", " + getName(field) + ")");
		}
	}

}
