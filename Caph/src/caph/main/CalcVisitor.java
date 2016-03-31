package caph.main;


public abstract class CalcVisitor {
	public abstract Object visit(Add node);

	public abstract Object visit(Mul node);

	public abstract Object visit(Equals node);

	public abstract Object visit(NotEquals node);

	public abstract Object visit(GreaterThan node);

	public abstract Object visit(GreaterThanEquals node);

	public abstract Object visit(LessThan node);

	public abstract Object visit(LessThanEquals node);

	public abstract Object visit(Int node);

	public abstract Object visit(Source node);

	public abstract Object visit(Vardecl node);
	
	public abstract Object visit(Monoral_bind node);
	
	public abstract Object visit(Parallel_bind node);
	
	public abstract Object visit(In node);

	public abstract Object visit(Out node);

	public abstract Object visit(Funcdecl node);

	public abstract Object visit(Name node);

	public abstract Object visit(Arglist node);

	public abstract Object visit(Arglist2 node);

	public abstract Object visit(Returnlist node);

	public abstract Object visit(Return node);

	public abstract Object visit(OthwiseRet node);

	public abstract Object visit(Returncase node);

	public abstract Object visit(Where node);

	public abstract Object visit(Declist node);

	public abstract Object visit(FuncCall node);

	public abstract Object visit(And node);

	public abstract Object visit(Or node);

	public abstract Object visit(Minus node);

	public abstract Object visit(Not node);

	public abstract Object visit(Bool node);//trueとfalseを統合
	
	//Lambda expression
	public abstract Object visit(Lambda node);
	
	//public abstract Object visit(Monoral_lambda node);
	
	//public abstract Object visit(Parallel_lambda node);
}