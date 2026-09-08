package net.amygdalum.xrayinterface.examples.tree.assertj;

import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static net.amygdalum.xrayinterface.examples.tree.assertj.TreeNodeTest.TreeNodeConsumer.treeNodeWithId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;
import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.examples.tree.TreeNode;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.tree.hamcrest.TreeNodeTest} - the same scenarios asserted with assertjs satisfies methods and
 * xrayinterfaces {@link IsEquivalent#isEquivalent(Class)} consumers
 */
public class TreeNodeTest {

	@Test
	public void testStepwise() throws Exception {
		TreeNode root = createTree();

		assertThat(root.getId()).isEqualTo("root");
		assertThat(root.getChildren()
			.get(0).getId()).isEqualTo("a");
		assertThat(root.getChildren()
			.get(0).getChildren()
			.get(0).getId()).isEqualTo("b");
		assertThat(root.getChildren()
			.get(0).getChildren()
			.get(1).getId()).isEqualTo("c");
		assertThat(root.getChildren()
			.get(1).getId()).isEqualTo("d");
	}

	@Test
	public void testEquals() throws Exception {
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

		assertThat(root).isEqualTo(expected);
	}

	@Test
	public void testXRay() throws Exception {
		TreeNode root = createTree();

		assertThat(root).satisfies(treeNodeWithId("root")
			.withChildren(hasSize(2)));
		assertThat(root.getChildren()).satisfiesExactly(
			treeNodeWithId("a").withChildren(hasSize(2)),
			treeNodeWithId("d").withChildren(hasSize(0)));
		assertThat(root.getChildren().get(0).getChildren()).satisfiesExactly(
			treeNodeWithId("b"),
			treeNodeWithId("c"));
	}

	@Test
	public void testXRayInAnyOrder() throws Exception {
		TreeNode root = createTree();

		assertThat(root.getChildren()).satisfiesExactlyInAnyOrder(
			treeNodeWithId("d"),
			treeNodeWithId("a"));
	}

	@Test
	public void testXRayMismatch() throws Exception {
		TreeNode root = createTree();

		assertThatThrownBy(() -> assertThat(root.getChildren()).satisfiesExactly(
			treeNodeWithId("d"),
			treeNodeWithId("a")))
				.isInstanceOf(AssertionError.class);
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

	interface TreeNodeConsumer extends Consumer<TreeNode> {

		TreeNodeConsumer withId(String id);

		TreeNodeConsumer withChildren(Matcher<Collection<? extends Object>> children);

		static TreeNodeConsumer treeNodeWithId(String id) {
			return isEquivalent(TreeNodeConsumer.class).withId(id);
		}

	}

}
