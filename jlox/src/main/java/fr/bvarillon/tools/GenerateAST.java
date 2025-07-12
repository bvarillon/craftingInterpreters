package fr.bvarillon.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * GenerateAST
 */
public class GenerateAST {
    public static void main(String[] args)throws IOException {
        if(args.length != 1) {
            System.err.println("Usage GenerateAST <output folder>");
            System.exit(64);
        }
        String output_dir = args[0];
        defineAst(output_dir, "Expr", Arrays.asList(
            "Binary : Expr left, Token operator, Expr right",
            "Grouping   : Expr exppression",
            "Literal    : Object value",
            "Unary      : Token operator, Expr right"
        ));
    } 

    private static void defineAst(String output_dir, String baseName, List<String> types) throws IOException {
        String path = output_dir + "/" + baseName + ".java";        
        PrintWriter writer = new PrintWriter(path);

        writer.println("package fr.bvarillon.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim(); 
            String fields = type.split(":")[1].trim();
                defineType(writer, baseName,className, fields);
        }


        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + "(" + typeName + " " + baseName.toUpperCase() + ");");
        }

        writer.println("    }");
        writer.println();
    }
 
    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println("// Class " + className);
        writer.println("    static class " + className + " extends " + baseName + " {");

        writer.println("    " + className + "(" + fields + ") {");
        String[] fieldsList = fields.split(", ");
        for (String field : fieldsList) {
            String name = field.split(" ")[1];
            writer.println("        this." + name + " = " + name + ";");
        }
        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("        return visitor.visit(this);");
        writer.println("    }");


        writer.println();
        for (String field : fieldsList) {
            writer.println("        final " + field + ";");
        }
        
        writer.println("    }");
        writer.println();
    }
}
