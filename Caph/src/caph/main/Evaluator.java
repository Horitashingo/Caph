package caph.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Evaluator extends CalcVisitor {

	HashMap<String, Object> record = new HashMap<String, Object>();
	Boolean record_ref;
	Boolean paraFlag = false;
	Boolean fcFlag = false;

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

		if (!record.containsKey(id)) {
			CalcTree returnListNode = node.child.get(2);
			for (int i = 0; i < returnListNode.child.size(); i++) {
				CalcTree returnNode = returnListNode.child.get(i);
				if (returnNode.child.get(0) instanceof Parallel_bind) {
					//System.out.println("line38" + returnNode.child.get(0).accept(this));//test
					System.err.println(id+(i+1)+"行目の木構造");
					CalcTree newTree = CalcTree.class.cast(returnNode.child.get(0).accept(this));// **
					returnNode.child.set(0, newTree);
					returnListNode.child.set(i, returnNode);
				}
			}
			//************
			if (node.child.size() == 4) {
				CalcTree whereNode = node.child.get(3);
				CalcTree declistNode = whereNode.child.get(0);
				for (int i = 0; i < declistNode.child.size(); i++) {
					CalcTree declNode = declistNode.child.get(i);
					if (declNode.child.get(1) instanceof Parallel_bind) {
						System.err.println(id+" where"+(i+1)+"行目の木構造");
						CalcTree newTree = CalcTree.class.cast(declNode.child
								.get(1).accept(this));// **
						declNode.child.set(1, newTree);
						declistNode.child.set(i, declNode);
						whereNode.child.set(0,declistNode);
						}
				}
				node.child.set(3, whereNode);
			}
			//************
			node.child.set(2, returnListNode);

			if(record.put(id, node) != null){
				System.err.println("you can't do destructive assignment 61" + id);
				System.exit(-1);
			}

		} else if(fcFlag){
			record_ref = true;
			return node;
		}
		else {
			System.err.println("you can't do destructive assignment 69" + id);
			System.exit(-1);
		}
		record_ref = true;
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
		Object ret = node.child.get(0).accept(this);
		return ret;
	}

	@Override
	public Object visit(Returncase node) {
		Boolean buff = true;
		for (int i = 0; i < node.child.size(); i++) {
			buff &= Boolean.class.cast(node.child.get(i).accept(this));
		}
		return buff;
	}

	@Override
	public Object visit(Where node) {
		for (int i = 0; i < node.child.size(); i++) {
			node.child.get(i).accept(this);
		}
		return null;
	}

	@Override
	public Object visit(Declist node) {
		node.child.get(0).accept(this);
		node.child.get(1).accept(this);
		return null;
	}

	@Override
	public Object visit(FuncCall node) {
		fcFlag = true;
		HashMap<String, Object> buff = new HashMap<String, Object>(record);
		@SuppressWarnings("unchecked")
		List<CalcTree> arg2 = (List<CalcTree>) node.child.get(1).accept(this);
		Funcdecl cnode = Funcdecl.class.cast(node.child.get(0).accept(this));
		@SuppressWarnings("unchecked")
		List<CalcTree> arg = (List<CalcTree>) cnode.child.get(1).accept(this);
		Object ret;

		HashMap<String, Object> buff2 = new HashMap<String, Object>();

		// 新環境の構築
		record_ref = false;

		// 関数自身を環境に追加
		buff2.put(String.class.cast(cnode.child.get(0).accept(this)), cnode);

		// 引数を環境に追加
		for (int i = 0; i < arg.size(); i++) {
			String id = String.class.cast(arg.get(i).accept(this));
			record_ref = true;
			Object val = arg2.get(i).accept(this);
			if(buff2.put(id, val) != null){
				System.err.println("you can't do destructive assignment 161");
				System.exit(-1);
			}
			record_ref = false;

		}

		record = buff2;

		// Where内の変数を追加
		if (cnode.child.size() == 4)
			cnode.child.get(3).accept(this);// Where

		record_ref = true;
		// 新環境の終了

		ret = cnode.child.get(2).accept(this);// Return

		record = buff;// 環境を元に戻す
		fcFlag=false;
		return ret;
	}

	@Override
	public Object visit(Add node) {
		//System.out.println("line189 " + paraFlag);//test
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		//System.out.println("line191 " + left+ ", " + right);//test
		//System.out.println("line192 " + paraFlag);//test
		if (paraFlag) {
			if (left instanceof Integer) {
				if (right instanceof Integer) {
					// 今までのAdd
					return Integer.class.cast(left) + Integer.class.cast(right);
				} else if (right instanceof HashMap<?, ?>) {
					HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
					LinkedList<String> key = new LinkedList<String>();
					key.add("*");
					map.merge(key, Integer.class.cast(left), (x, y) -> x + y);
					return map;
				} else if (right instanceof String) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> newRight = new LinkedList<String>();
					LinkedList<String> newLeft = new LinkedList<String>();
					newRight.add(String.class.cast(right));
					map.put(newRight, 1);
				    ///
					newLeft.add("*");
					map.put(newLeft, Integer.class.cast(left));
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else if (left instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) left;
				if (right instanceof Integer) {
					LinkedList<String> key = new LinkedList<String>();
					key.add("*");
					map.merge(key, Integer.class.cast(right), (x, y) -> x + y);
					return map;
				} else if (right instanceof HashMap<?, ?>) {
					HashMap<LinkedList<String>, Integer> map2 = (HashMap<LinkedList<String>, Integer>) right;
					for (LinkedList<String> key2 : map2.keySet()) {
						map.merge(key2, map2.get(key2), (x, y) -> x + y);
					}
					return map;
				} else if (right instanceof String) {
					LinkedList<String> key = new LinkedList<String>();
					key.add(String.class.cast(right));
					//System.out.println(map);//test
					map.merge(key, 1, (x, y) -> x + y);
					//System.out.println(map);//test
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else if (left instanceof String) {
				if (right instanceof Integer) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> newLeft = new LinkedList<String>();
					LinkedList<String> newRight = new LinkedList<String>();
					newLeft.add(String.class.cast(left));
					map.put(newLeft, 1);
					newRight.add("*");
					map.put(newRight, Integer.class.cast(right));
					return map;
				} else if (right instanceof HashMap<?, ?>) {
					Map<LinkedList<String>, Integer> map = (Map<LinkedList<String>, Integer>) right;
					LinkedList<String> key = new LinkedList<String>();
					key.add(String.class.cast(left));
					map.merge(key, 1, (x, y) -> x + y);
					return map;
				} else if (right instanceof String) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> key1 = new LinkedList<String>();
					LinkedList<String> key2 = new LinkedList<String>();
					key1.add(String.class.cast(left));
					map.put(key1, 1);

					key2.add(String.class.cast(right));
					map.merge(key2, 1, (x, y) -> x + y);
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else {
				System.err.println("error");
				return null;
			}
		} else {
			//System.out.println(left + ", " + right);//test
			return Integer.class.cast(left) + Integer.class.cast(right);
		}

	}

	@Override
	public Object visit(Mul node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);

		//System.out.println("line287 " + paraFlag);//test
		if (paraFlag) {
			if (left instanceof Integer) {
				if (right instanceof Integer) {
					// 今までのMultiple
					return Integer.class.cast(left) * Integer.class.cast(right);
				} else if (right instanceof HashMap<?, ?>) {
					HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
					map.forEach((key, value) -> map.merge(key,
							Integer.class.cast(left),
							(oldValue, newValue) -> oldValue * newValue));
					return map;
				} else if (right instanceof String) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> newRight = new LinkedList<String>();
					newRight.add(String.class.cast(right));
					map.put(newRight, Integer.class.cast(left));
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else if (left instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) left;
				if (right instanceof Integer) {
					map.forEach((key, value) -> map.merge(key,
							Integer.class.cast(right),
							(oldValue, newValue) -> oldValue * newValue));
					return map;
				} else if (right instanceof HashMap<?, ?>) {
					HashMap<LinkedList<String>, Integer> map2 = (HashMap<LinkedList<String>, Integer>) right;
					for (LinkedList<String> key : map.keySet()) {
						if (key.remove("*")) {
							for (LinkedList<String> key2 : map2.keySet()) {
								key.addAll(key2);
								map.merge(key, map2.get(key2), (x, y) -> x * y);
							}
						} else {
							for (LinkedList<String> key2 : map2.keySet()) {
								if (key2.remove("*")) {
									map.merge(key, map2.get(key2), (x, y) -> x
											* y);
								} else {
									key.addAll(key2);
									map.merge(key, map2.get(key2), (x, y) -> x
											* y);
									Collections.sort(key);
								}
							}
						}
					}
					return map;
				} else if (right instanceof String) {
					String newRight = String.class.cast(right);
					for (LinkedList<String> key : map.keySet()) {
						if (key.remove("*")) {
							key.add(newRight);
						} else {
							key.add(newRight);
							Collections.sort(key);
						}
					}
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else if (left instanceof String) {
				if (right instanceof Integer) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> newLeft = new LinkedList<String>();
					newLeft.add(String.class.cast(left));
					map.put(newLeft, Integer.class.cast(right));
					return map;
				} else if (right instanceof HashMap<?, ?>) {
					HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
					String newLeft = String.class.cast(left);
					for (LinkedList<String> key : map.keySet()) {
						if (key.remove("*")) {
							key.add(newLeft);
						} else {
							key.add(newLeft);
							Collections.sort(key);
						}
					}
					return map;
				} else if (right instanceof String) {
					Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
					LinkedList<String> newKey = new LinkedList<String>();
					newKey.add(String.class.cast(left));
					newKey.add(String.class.cast(right));
					Collections.sort(newKey);
					map.put(newKey, 1);
					return map;
				} else {
					System.err.println("error");
					return null;
				}
			} else {
				System.err.println("error");
				return null;
			}

		} else {
			//System.out.println("line390 " + left + ", " + right);//test
			return Integer.class.cast(left) * Integer.class.cast(right);
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
		if(node.child.get(1) instanceof Parallel_bind){
			System.err.println(id+"の木構造");
		}
		Object val = node.child.get(1).accept(this);

		if(record.put(id, val) != null){
			System.err.println("you can't do destructive assignment 477");
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
			System.err.println("you can't do destructive assignment 490");
			System.exit(-1);
		}
		@SuppressWarnings("resource")
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
		fcFlag = true;
		Object output = node.child.get(0).accept(this);
		System.out.println("result = " +output+"\n");
		fcFlag = false;
		return null;
	}

	@Override
	public Object visit(Name node) {
		if (record.containsKey(node.str) && record_ref) {
			Object value = record.get(node.str);
			if (value instanceof CalcTree) {
				//System.out.println("?" + ((CalcTree) value).accept(this));
				Object val = ((CalcTree) value).accept(this);
				record.replace(node.str, val);
			}
			return record.get(node.str);
		}
		//System.out.println(node.str + record.entrySet() + node);
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

	@Override
	public Object visit(Monoral_bind node) {
		if(fcFlag) return node.child.get(0).accept(this);
		return node.child.get(0);
	}

	@Override
	public Object visit(Parallel_bind node) {
		paraFlag = true;
		Object nodeZero = node.child.get(0).accept(this);
		//System.out.println("line564 " + nodeZero);//test
		if (nodeZero instanceof Integer) {
			paraFlag = false;
			System.err.println(nodeZero);
			return Integer.class.cast(nodeZero);
		} else {
			//System.out.println("line570 " + nodeZero);//test
			HashMap<LinkedList<String>, Integer> expressionData = (HashMap<LinkedList<String>, Integer>) nodeZero;
			LinkedList<CalcTree> tree2add = new LinkedList<CalcTree>();
			System.err.println(expressionData);//test
			for (LinkedList<String> key : expressionData.keySet()) {
				if (key.size() == 1) {
					if (String.class.cast(key.get(0)).equals("*")) {
						CalcTree value = new Int(
								Integer.class.cast(expressionData.get(key)));
						//System.out.println("ほ" + Integer.class.cast(expressionData.get(key)));//test
						tree2add.add(value);
					} else {
						CalcTree left = new Name(String.class.cast(key.get(0)));
						CalcTree right = new Int(
								Integer.class.cast(expressionData.get(key)));
						CalcTree newMul = new Mul(left, right);
						//System.out.println(String.class.cast(key.get(0)) + "," + Integer.class.cast(expressionData.get(key)) );//test
						tree2add.add(newMul);
					}
				} else {
					CalcTree newMul = new Mul(null, null);
					for (int i = 0; i < key.size(); i++) {
						if (i == 0) {
							CalcTree left = new Name(String.class.cast(key
									.get(i)));
							CalcTree right = new Name(String.class.cast(key
									.get(i + 1)));
							newMul = new Mul(left, right);
							//System.out.println(key + "," + key.get(i) );//test
							i++;
						} else {
							CalcTree right = new Name(String.class.cast(key
									.get(i)));
							newMul = new Mul(newMul, right);
							//System.out.println(key + "," + key.get(i) );//test
						}
					}
					CalcTree val = new Int(Integer.class.cast(expressionData
							.get(key)));
					//System.out.println(Integer.class.cast(expressionData.get(key)));//test
					newMul = new Mul(newMul, val);
					tree2add.add(newMul);
				}
			}
			if (tree2add.size() == 1) {
				//System.out.println("line616");//test
				paraFlag = false;
				return tree2add.get(0);
			} else {
				CalcTree newTree = new Add(null, null);
				for (int i = 0; i < tree2add.size(); i++) {
					if (i == 0) {
						newTree = new Add(tree2add.get(i), tree2add.get(i + 1));
						i++;
					} else {
						newTree = new Add(newTree, tree2add.get(i));
					}
				}
				//System.out.println("line629");//test
				paraFlag = false;
				return newTree;
			}
		}
	}


	@Override
	public Object visit(Lambda node) {
		Boolean fcFlag_buff = new Boolean(fcFlag);
		Boolean paraFlag_buff = new Boolean(paraFlag);
		//System.out.println("line638 " + paraFlag);//test

		//System.out.println("636record " + record);//test

		HashMap<String, Object> buff = new HashMap<String, Object>(record);
		@SuppressWarnings("unchecked")
		List<CalcTree> arg2 = (List<CalcTree>) node.child.get(2).child;
		@SuppressWarnings("unchecked")
		List<CalcTree> arg = (List<CalcTree>) node.child.get(0).child;
		Object ret;

		fcFlag = false;
		record_ref = false;

		//Collection<Callable<Object>> processes = new LinkedList<Callable<Object>>();

		if (node.child.get(1) instanceof Parallel_bind) System.err.println("λ式の木構造");
		CalcTree newTree = CalcTree.class.cast(node.child.get(1).accept(this));
		node.child.get(1).child.set(0, newTree);
		//node.child.set(1, newTree);

		// 引数を環境に追加
		for (int i = 0; i < arg.size(); i++) {
		String id = String.class.cast(arg.get(i).accept(this));
			record_ref = true;
			Object val = arg2.get(i).accept(this);
			if(record.put(id, val) != null){
				System.err.println("you can't do destructive assignment 144");
				System.exit(-1);
			}
			record_ref = false;
		}

		record_ref = true;
		fcFlag = true;

		paraFlag = new Boolean(paraFlag_buff);
		//System.out.println("line679 " + paraFlag);//test
		ret = newTree.accept(this);
		//System.out.println("!" + ret);//test
		record = buff;
		//System.out.println("673record " + record);//test
		fcFlag = fcFlag_buff;
		paraFlag = paraFlag_buff;
		//System.out.println("line682 " + paraFlag);//test
		//System.out.println("line679 " + ret);
		return ret;
	}

}
