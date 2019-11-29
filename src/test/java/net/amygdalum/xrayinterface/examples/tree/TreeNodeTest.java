package net.amygdalum.xrayinterface.examples.tree;

import static net.amygdalum.xrayinterface.examples.tree.TreeNodeTest.TreeNodeMatcher.treeNodeWithId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.hamcrest.Matcher;
import org.junit.Test;

import net.amygdalum.xrayinterface.IsEquivalent;

public class TreeNodeTest {

	@Test
	public void testStepwise() throws Exception {
		TreeNode root = createTree();

		assertThat(root.getId(), equalTo("root"));
		assertThat(root.getChildren()
			.get(0).getId(), equalTo("a"));
		assertThat(root.getChildren()
			.get(0).getChildren()
			.get(0).getId(), equalTo("b"));
		assertThat(root.getChildren()
			.get(0).getChildren()
			.get(1).getId(), equalTo("c"));
		assertThat(root.getChildren()
			.get(1).getId(), equalTo("d"));
	}

	@Test
	public void TestEquals() throws Exception {
		TreeNode root = createTree();

		TreeNode expected = new TreeNode("root", null);

		TreeNode a = new TreeNode("a", expected);
		expected.addChild(a);

		TreeNode b = new TreeNode("b", a);
		a.addChild(b);

		TreeNode c = new TreeNode("c", a);
		a.addChild(c);

		TreeNode d = new TreeNode("d", root);
		expected.addChild(d);

		assertThat(root, equalTo(expected));
	}

	@Test
	public void testXRay() throws Exception {
		TreeNode root = createTree();

		TreeNodeMatcher treeNodeWithId = treeNodeWithId("root");
		assertThat(root, treeNodeWithId
			.withChildrenContaining(
				treeNodeWithId("a")
					.withChildrenContaining(
						treeNodeWithId("b"),
						treeNodeWithId("c")),
				treeNodeWithId("d")));
	}

	private TreeNode createTree() {
		TreeNode root = new TreeNode("root", null);

		TreeNode a = new TreeNode("a", root);
		root.addChild(a);

		TreeNode b = new TreeNode("b", a);
		a.addChild(b);

		TreeNode c = new TreeNode("c", a);
		a.addChild(c);

		TreeNode d = new TreeNode("d", root);
		root.addChild(d);
		return root;
	}

	interface TreeNodeMatcher extends org.hamcrest.Matcher<TreeNode> {
		public TreeNodeMatcher withId(String id);

		public TreeNodeMatcher withChildren(Matcher<Iterable<? extends TreeNode>> c);

		public default TreeNodeMatcher withChildrenContaining(TreeNodeMatcher... c) {
			return withChildren(contains(c));
		}
		
		public static TreeNodeMatcher treeNodeWithId(String id) {
			return IsEquivalent.equivalentTo(TreeNodeMatcher.class).withId(id);
		}
	}
	
	

}
