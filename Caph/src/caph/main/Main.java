package caph.main;

import java.io.IOException;

import nez.Parser;
import nez.ast.CommonTree;
import nez.main.CommandContext;


public class Main {

	public final static void main(String[] args) {
		try {
			CommandContext c = new CommandContext();
			c.parseCommandOption(args, false/* nezCommand */);
			Parser p = c.newParser();
			CommonTree node = p.parseCommonTree(c.nextInput());
			if(node != null){
				CalcTree cnode = Translator.translate(node);
				Evaluator evaluator = new Evaluator();
				evaluator.eval(cnode);
			}
		} catch (IOException e) {
			System.exit(1);
		}
	}

}