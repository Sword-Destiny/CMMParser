package com.yuanhonglong.source_editor;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * 图标 <br>
 * 
 * @author 天命剑主<br>
 *         on 2015/11/7.
 */
public class MyIcons {
	public static Icon	ICON_K;
	public static Icon	ICON_V;
	public static Icon	ICON_F;
	public static Icon	ICON_P;
	public static Icon	RED_CIRCLE;
	public static Icon	YELLOW_CIRCLE;
	public static Icon	GRAY_CIRCLE;
	public static Icon	RED_RECTANGLE;
	public static Icon	YELLOW_RECTANGLE;
	public static Icon	RIGHT;
	public static Icon	RUN_ITEM;
	public static Icon	STOP_ITEM;
	public static Icon	DEBUG_ITEM;
	public static Icon	EXIT;
	public static Icon	REMOVE_ALL;
	public static Icon	STEP_1;
	public static Icon	STEP_2;
	public static Icon	STEP_3;
	public static Icon	STEP_4;
	public static Icon	G;
	public static Icon	L;
	public static Icon	J;
	public static Icon	M;

	static {
		Image image;
		try {
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/k.png"));
			MyIcons.ICON_K = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/v.png"));
			MyIcons.ICON_V = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/f.png"));
			MyIcons.ICON_F = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/p.png"));
			MyIcons.ICON_P = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/warning.png"));
			MyIcons.YELLOW_CIRCLE = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/error.png"));
			MyIcons.RED_CIRCLE = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/red.png"));
			MyIcons.RED_RECTANGLE = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/yellow.png"));
			MyIcons.YELLOW_RECTANGLE = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/gray.png"));
			MyIcons.GRAY_CIRCLE = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/right.png"));
			MyIcons.RIGHT = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/run_item.png"));
			MyIcons.RUN_ITEM = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/stop_item.png"));
			MyIcons.STOP_ITEM = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/debug_item.png"));
			MyIcons.DEBUG_ITEM = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/exit.png"));
			MyIcons.EXIT = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/remove_all.png"));
			MyIcons.REMOVE_ALL = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/1.png"));
			MyIcons.STEP_1 = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/2.png"));
			MyIcons.STEP_2 = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/3.png"));
			MyIcons.STEP_3 = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/4.png"));
			MyIcons.STEP_4 = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/m.png"));
			MyIcons.M = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/j.png"));
			MyIcons.J = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/l.png"));
			MyIcons.L = new ImageIcon(image);
			image = ImageIO.read(MyIcons.class.getResourceAsStream("/com/yuanhonglong/icons/g.png"));
			MyIcons.G = new ImageIcon(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
