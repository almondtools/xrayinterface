package net.amygdalum.xrayinterface.examples.tree;

import java.util.ArrayList;
import java.util.List;

public 	class TreeNode {
	private String id;
	private TreeNode parent;
	private List<TreeNode> children;

	public TreeNode(String id, TreeNode parent) {
		this.id = id;
		this.parent = parent;
		this.children = new ArrayList<>();
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

	public String getId() {
		return id;
	}

	public TreeNode getParent() {
		return parent;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object obj) {
		TreeNode that = (TreeNode) obj;
		return this.id.equals(that.id)
			&& this.children.equals(that.children);
	}

}