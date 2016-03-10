package com.yuanhonglong.source_editor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarError;
import com.yuanhonglong.cmm.MainFrame;

/**
 * 错误提示窗口
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class ErrorWindow extends JWindow {

	private static final long		serialVersionUID	= -8412184622781707052L;
	public JScrollPane				errorScrollPane;							// 树显示面板滚动条
	public JPanel					errorPanel;									// 树显示面板
	public JTree					errorTree;									// 错误提示树
	DefaultMutableTreeNode			errorWarning;								// 错误提示树根节点
	public ErrorTreeCellRenderer	renderer;									// 树显示样式

	public static final int			WIDTH				= 300;					// 窗口宽
	public static final int			HEIGHT				= 90;					// 窗口高
	public static final Font		YAHEI_11;									// 雅黑字体

	static {
		YAHEI_11 = new Font("Microsoft YaHei UI", Font.PLAIN, SourceTextPane.SMALL_TEXT_SIZE);
		UIManager.put("Tree.textBackground", MainFrame.LIGHT_GRAY_COLOR);
	}

	public ErrorWindow(MainFrame frame) {
		super(frame);
		setSize(WIDTH, HEIGHT);
		errorPanel = new JPanel();
		errorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		errorScrollPane = new JScrollPane(errorPanel);
		errorScrollPane.setSize(WIDTH, HEIGHT);
		errorScrollPane.setLocation(0, 0);
		errorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		errorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(errorScrollPane);

		renderer = new ErrorTreeCellRenderer();

		errorWarning = new DefaultMutableTreeNode("错误/警告");
		errorTree = new JTree(errorWarning);
		errorTree.setCellRenderer(renderer);
		errorTree.setAutoscrolls(true);
		errorTree.setBackground(MainFrame.LIGHT_GRAY_COLOR);
		errorPanel.add(errorTree);

		addMouseListener(new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				ErrorWindow.this.setVisible(false);
			}
		});
	}

	/**
	 * 展开树的所有节点
	 */
	public void expandTree(JTree tree) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
		expandAll(tree, new TreePath(node), true);
	}

	/**
	 * 展开树的所有节点
	 */
	public void expandAll(JTree tree, TreePath parent, boolean expand) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	/**
	 * 清空树
	 */
	public void clear() {
		JScrollBar hBar = errorScrollPane.getHorizontalScrollBar();
		JScrollBar vBar = errorScrollPane.getVerticalScrollBar();
		if (hBar.isVisible()) {
			hBar.setValue(0);
		}
		if (vBar.isVisible()) {
			vBar.setValue(0);
		}
		errorPanel.remove(errorTree);
		errorWarning.removeAllChildren();
		errorTree = new JTree(errorWarning);
		errorTree.setCellRenderer(renderer);
		errorTree.setAutoscrolls(true);
		errorTree.setBackground(MainFrame.LIGHT_GRAY_COLOR);
		errorPanel.add(errorTree);
	}

	/**
	 * 添加语法错误菜单项
	 *
	 * @param e
	 *            语法错误
	 */
	public void addItem(GrammarError e) {
		DefaultMutableTreeNode item = new DefaultMutableTreeNode(e);
		errorWarning.add(item);
		expandTree(errorTree);
	}

	/**
	 * 添加词法错误菜单项
	 *
	 * @param lexicalError
	 *            语法错误
	 */
	public void addItem(String lexicalError) {
		DefaultMutableTreeNode item = new DefaultMutableTreeNode(lexicalError);
		errorWarning.add(item);
		expandTree(errorTree);
	}

	/**
	 * 树显示的样式
	 *
	 * @author 天命剑主 <br>
	 *         create by eclipse<br>
	 *         on 2015年11月14日 <br>
	 */
	public class ErrorTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 5307129033313528119L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object v, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, v, selected, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode value = (DefaultMutableTreeNode) v;
			Object obj = value.getUserObject();
			if (obj instanceof DefaultMutableTreeNode) {
				obj = ((DefaultMutableTreeNode) obj).getUserObject();
			}
			if (obj instanceof String) {
				String string = obj.toString();
				if (string.equals("错误/警告")) {
					setIcon(MyIcons.RED_CIRCLE);
					setText(value.toString());
				} else {
					setIcon(MyIcons.RED_CIRCLE);
					setText("error: " + value.toString());
				}
			} else if (obj instanceof GrammarError) {
				GrammarError e = (GrammarError) obj;
				if (e.isWarning) {
					setIcon(MyIcons.YELLOW_CIRCLE);
					setText("warning: " + e.toString());
				} else {
					setIcon(MyIcons.RED_CIRCLE);
					setText("error: " + e.toString());
				}
			} else {
				setText(value.toString());
			}
			setFont(YAHEI_11);
			return this;
		}
	}
}
