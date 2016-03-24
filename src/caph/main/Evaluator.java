package caph.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Evaluator extends CalcVisitor {

	HashMap<String, Object> record = new HashMap<String, Object>();
	Boolean record_ref;

	public Object eval(CalcTree node) {
		return node.accept(this);
	}

	@Override
	public Object visit(Source node) {
		record_ref = true;
		for (int i = 0; i < node.child.size(); i++) {
			Object buff = node.child.get(i).accept(this);
			if (buff != null)
				System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(Funcdecl node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		if (!record.containsKey(id))
			record.put(id, node);
		else {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(Arglist node) {
		return node.child;
	}

	@Override
	public Object visit(Arglist2 node) {
		return node.child;
	}

	@Override
	public Object visit(Returnlist node) {
		Object ret = null;

		for (int i = 0; i < node.child.size(); i++) {
			ret = node.child.get(i).accept(this);
			if (ret != null)
				break;
		}

		return ret;
	}

	@Override
	public Object visit(Return node) {
		if (Boolean.class.cast(node.child.get(1).accept(this)))
			return node.child.get(0).accept(this);
		else
			return null;
	}

	@Override
	public Object visit(OthwiseRet node) {
		return node.child.get(0).accept(this);
	}

	@Override
	public Object visit(Returncase node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left && right;
	}

	@Override
	public Object visit(Where node) {
		return node.child.get(0).accept(this);
	}

	@Override
	public Object visit(Declist node) {
		node.child.get(0).accept(this);
		node.child.get(1).accept(this);
		return null;
	}

	@Override
	public Object visit(FuncCall node) {
		HashMap<String, Object> buff = new HashMap<String, Object>(record);
		@SuppressWarnings("unchecked")
		List<CalcTree> arg2 = (List<CalcTree>) node.child.get(1).accept(this);
		Funcdecl cnode = Funcdecl.class.cast(node.child.get(0).accept(this));
		@SuppressWarnings("unchecked")
		List<CalcTree> arg = (List<CalcTree>) cnode.child.get(1).accept(this);
		List<Object> arg2_val = new ArrayList<Object>();
		Object ret;

		for (int i = 0; i < arg2.size(); i++) {
			arg2_val.add(arg2.get(i).accept(this));
		}

		record.clear();

		// 新環境の構築
		record_ref = false;

		// 関数自身を環境に追加
		record.put(String.class.cast(cnode.child.get(0).accept(this)), cnode);

		// 引数を環境に追加
		for (int i = 0; i < arg.size(); i++) {
			String id = String.class.cast(arg.get(i).accept(this));
			if (!record.containsKey(id))
				record.put(id, arg2_val.get(i));
			else {
				System.err.println("you can't do destructive assignment");
				System.exit(-1);
			}
		}

		// Where内の変数を追加
		if (cnode.child.size() == 4)
			cnode.child.get(3).accept(this);// Where

		record_ref = true;
		// 新環境の終了

		ret = cnode.child.get(2).accept(this);// Return

		record = buff;// 環境を元に戻す
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(Add node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left instanceof Integer && right instanceof Integer) {
			return Integer.class.cast(left) + Integer.class.cast(right);
		}// 今までのAdd
		else {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			if (right instanceof HashMap<?, ?>) {
				map.putAll((Map<? extends String, ? extends Integer>) right);
			}
			if (left instanceof HashMap<?, ?>) {
				HashMap<String, Integer> map2 = (HashMap<String, Integer>) left;
				for (String key : map2.keySet()) {
					if (map.containsKey(key)) {
						map.merge(key, map2.get(key),
								(oldValue, newValue) -> oldValue + newValue);
					} else {
						map.put(key, map2.get(key));
					}
				}
			} else if (left instanceof String) {
				String left2 = String.class.cast(left);
				if (map.containsKey(left2)) {
					map.merge(left2, 1, (oldValue, newValue) -> oldValue
							+ newValue);
				} else {
					map.put(left2, 1);
				}
			} else if (left instanceof Integer) {
				if (map.containsKey("const")) {
					map.merge("const", Integer.class.cast(left), (oldValue,
							newValue) -> oldValue + newValue);
				} else {
					map.put("const", Integer.class.cast(left));
				}
			}
			if (right instanceof String) {
				String right2 = String.class.cast(right);
				if (map.containsKey(right2)) {
					map.merge(right2, 1, (oldValue, newValue) -> oldValue
							+ newValue);
				} else {
					map.put(right2, 1);
				}
			} else if (right instanceof Integer) {
				if (map.containsKey("const")) {
					map.merge("const", Integer.class.cast(right), (oldValue,
							newValue) -> oldValue + newValue);
				} else {
					map.put("const", Integer.class.cast(right));
				}
			}
			return map;
		}
	}

	@Override
	public Object visit(Mul node) {
		Object left = Object.class.cast(node.child.get(0).accept(this));
		Object right = Object.class.cast(node.child.get(1).accept(this));
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return Integer.class.cast(left) * Integer.class.cast(right);
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<String, Integer> map = (HashMap<String, Integer>) right;
				map.forEach((key, value) -> map.merge(key,
						Integer.class.cast(left),
						(oldValue, newValue) -> oldValue * newValue));
				return map;
			} else if (right instanceof String) {
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				map.put(String.class.cast(right), Integer.class.cast(left));
				return map;
			} else {
				System.err.println("error");
				return null;
			}
		} else if (right instanceof Integer) {
			if (left instanceof HashMap<?, ?>) {
				HashMap<String, Integer> map = (HashMap<String, Integer>) right;
				map.forEach((key, value) -> map.merge(key,
						Integer.class.cast(right),
						(oldValue, newValue) -> oldValue * newValue));
				return map;
			} else if (left instanceof String) {
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				map.put(String.class.cast(left), Integer.class.cast(right));
				return map;
			} else {
				System.err.println("error");
				return null;
			}
		} else {
			System.err.println("error");
			return null;
		}
	}

	@Override
	public Object visit(Int node) {
		return node.val;
	}

	@Override
	public Object visit(Equals node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left == right) {
			return true;
		}
		return false;
	}

	public Object visit(NotEquals node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left == right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(GreaterThan node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left > right) {
			return true;
		}
		return false;
	}

	@Override
	public Object visit(GreaterThanEquals node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(LessThan node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return true;
		}
		return false;
	}

	@Override
	public Object visit(LessThanEquals node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(And node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left && right;
	}

	@Override
	public Object visit(Or node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left || right;
	}

	@Override
	public Object visit(Vardecl node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		Object val = node.child.get(1).accept(this);
		if (!record.containsKey(id))

			record.put(id, val);
		else {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(In node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		if (record.containsKey(id)) {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		Scanner scan = new Scanner(System.in);
		System.err.println("please input \"" + id + "\"");
		String in = scan.next();
		switch (in) {
		case "true":
			record.put(id, true);
			break;
		case "false":
			record.put(id, false);
			break;
		default:
			record.put(id, Integer.parseInt(in));
			break;
		}
		return null;
	}

	@Override
	public Object visit(Out node) {
		System.out.println(node.child.get(0).accept(this));
		return null;
	}

	@Override
	public Object visit(Name node) {
		if (record.containsKey(node.str) && record_ref)
			return record.get(node.str);
		return node.str;
	}

	@Override
	public Object visit(Bool node) {
		return node.bool;
	}

	@Override
	public Object visit(Minus node) {
		return -1 * Integer.class.cast(node.child.get(0).accept(this));
	}

	@Override
	public Object visit(Not node) {
		return !Boolean.class.cast(node.child.get(0).accept(this));
	}

}
