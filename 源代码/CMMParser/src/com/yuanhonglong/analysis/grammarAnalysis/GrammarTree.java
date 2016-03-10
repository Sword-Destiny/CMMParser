package com.yuanhonglong.analysis.grammarAnalysis;

import java.util.ArrayList;

/**
 * 语法树
 *
 * @author 天命剑主<br>
 *         on 2015年10月17日
 */
public class GrammarTree {

	public ArrayList<GrammarTree>	children;	// 子节点
	public int						depth;		// 节点深度
	public String					value;		// 节点值
	public GrammarTree				parent;		// 父节点

	public GrammarTree(int depth, String value) {
		children = new ArrayList<>();
		this.depth = depth;
		this.value = value;
		this.parent = null;
	}

	public GrammarTree(int depth, String value, GrammarTree parent) {
		children = new ArrayList<>();
		this.depth = depth;
		this.value = value;
		this.parent = parent;
	}

	/**
	 * 返回最末的子树
	 */
	public GrammarTree lastChild() {
		return this.children.get(this.children.size() - 1);
	}

	/**
	 * 添加子树
	 */
	public void addChild() {
		GrammarTree tree = new GrammarTree(this.depth + 1, "", this);
		this.children.add(tree);
	}

	/**
	 * 添加子树
	 *
	 * @param value
	 *            子树的值
	 */
	public void addChild(String value) {
		GrammarTree tree = new GrammarTree(this.depth + 1, value, this);
		this.children.add(tree);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String string = value + "\n";
		if (this.children.size() == 0) {
			return string;
		}
		for (int i = 0; i < children.size(); i++) {
			GrammarTree tree = children.get(i);
			String childStr = tree.toString();
			String[] childsStrs = childStr.split("\n");
			if (i == (children.size() - 1)) {
				if (childsStrs.length > 0) {
					childsStrs[0] = "└─> " + childsStrs[0];
					for (int j = 1; j < childsStrs.length; j++) {
						childsStrs[j] = "    " + childsStrs[j];
					}
				}
			} else {
				if (childsStrs.length > 0) {
					childsStrs[0] = "├─> " + childsStrs[0];
					for (int j = 1; j < childsStrs.length; j++) {
						childsStrs[j] = "│   " + childsStrs[j];
					}
				}
			}
			for (String s : childsStrs) {
				string += s + "\n";
			}
		}
		return string;
	}
}
