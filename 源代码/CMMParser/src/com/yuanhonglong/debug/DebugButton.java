package com.yuanhonglong.debug;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * 自定义按钮
 * 
 * @author 天命剑主 <br />
 *         create by eclipse on 2015年11月12日 <br />
 */
public class DebugButton extends JButton {

	private static final long	serialVersionUID	= -9205870838712133744L;
	public static final Color	SELECTED_COLOR		= new Color(180, 230, 250, 179);
	public static final Color	HOVER_COLOR			= new Color(180, 230, 250, 255);
	public static final Color	BACKGROUND			= new Color(222, 222, 222);
	public boolean				hover;												// 是否鼠标悬停
	public boolean				selected;											// 是否选中

	public DebugButton(String text) {
		super(text);
		setBorderPainted(false);
		setFocusPainted(false);
		setContentAreaFilled(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hover = false;
				repaint();
			}
		});
	}

	public void select() {
		selected = true;
		repaint();
	}

	public void unselect() {
		selected = false;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		int w = getWidth();
		int h = getHeight();
		if (hover) {
			g2d.setColor(HOVER_COLOR);
			g2d.fillRect(0, 0, w, h);
		} else if (selected) {
			g2d.setColor(SELECTED_COLOR);
			g2d.fillRect(0, 0, w, h);
		} else {
			g2d.setColor(BACKGROUND);
			g2d.fillRect(0, 0, w, h);
		}
		g2d.dispose();
		super.paintComponent(g);
	}
}
