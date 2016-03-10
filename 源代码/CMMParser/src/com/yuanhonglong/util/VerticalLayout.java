package com.yuanhonglong.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * 这个布局文件是网上找到的 <br>
 * 经过了我的修改<br>
 * <a href="http://blog.csdn.net/dancen/article/details/7581971">url</a> <br>
 * <a href="http://my.csdn.net/Dancen">author</a> <br>
 */
public class VerticalLayout extends FlowLayout {
	private static final long serialVersionUID = 1L;

	@Override
	public Dimension preferredLayoutSize(Container target) {
		Dimension tarsiz = new Dimension(0, 0);
		int numcomp = target.getComponentCount();
		for (int i = 0; i < numcomp; i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				Dimension d = m.getSize();
				tarsiz.width = Math.max(tarsiz.width, d.width);
				if (i < (numcomp - 1)) {
					tarsiz.height += 1;
				}
				tarsiz.height += d.height;
			}
		}
		Insets insets = target.getInsets();
		tarsiz.width += insets.left + insets.right;
		tarsiz.height += insets.top + insets.bottom;
		return tarsiz;
	}

	/**
	 * Returns the minimum size needed to layout the target container.
	 *
	 * @param target
	 *            the component to lay out.
	 * @return the minimum layout dimension.
	 */
	@Override
	public Dimension minimumLayoutSize(Container target) {
		Dimension tarsiz = new Dimension(0, 0);
		int numcomp = target.getComponentCount();
		for (int i = 0; i < numcomp; i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				Dimension d = m.getMinimumSize();
				tarsiz.width = Math.max(tarsiz.width, d.width);
				if (i < (numcomp - 1)) {
					tarsiz.height += 1;
				}
				tarsiz.height += d.height;
			}
		}
		Insets insets = target.getInsets();
		tarsiz.width += insets.left + insets.right;
		tarsiz.height += insets.top + insets.bottom;
		return tarsiz;
	}

	/**
	 * Lays out the container.
	 *
	 * @param target
	 *            the container to lay out.
	 */
	@Override
	public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		int maxwidth = target.getSize().width - (insets.left + insets.right);
		int numcomp = target.getComponentCount();
		int x = insets.left, y = 0;
		for (int i = 0; i < numcomp; i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				Dimension d = m.getSize();
				m.setSize(maxwidth, d.height);
				d.width = maxwidth;
				m.setLocation(x, y);
				if (i < (numcomp - 1)) {
					y += 1;
				}
				y += d.height;
			}
		}
	}
}
