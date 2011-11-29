package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import baseModel.Game;

/**
 * @author AJ Venne Created on 8:08:05 PM Nov 26, 2011
 */
@SuppressWarnings("serial")
public class MasterView extends javax.swing.JFrame {
	/**
	 * @author Chris Ray Created on 8:07:48 PM Nov 26, 2011
	 */
	private Game game;
	private JPanel body;

	/**
	 * @author AJ Venne Created on 8:07:48 PM Nov 26, 2011
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		MasterView mv = new MasterView();
		mv.setVisible(true);
	}

	/**
	 * 
	 * @author AJ Venne Created on 8:08:19 PM Nov 26, 2011
	 * 
	 */
	public MasterView() {
		game = new Game();
		body = new JPanel();
		setUpGUICode();
		setUpMenuBar();
		setUpBody();
		changeView(Views.TITLE, null);
	}

	private void setUpGUICode() {
		this.setSize(1230, 685);
		this.setResizable(false);
		setLocation(10, 10);
		setTitle("Risk");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(body, BorderLayout.CENTER);

	}

	private void setUpMenuBar() {
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu about = new JMenu("About");
		JMenuItem exit = new JMenuItem("Exit");
		file.add(exit);
		menu.add(file);
		about.addMenuListener(new aboutListener());
		menu.add(about);
		exit.addActionListener(new exitListener());
		setJMenuBar(menu);
	}

	private class exitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent exitClick) {
			System.exit(0);
		}
	}

	private class aboutListener implements MenuListener {

		@Override
		public void menuCanceled(MenuEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void menuDeselected(MenuEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void menuSelected(MenuEvent e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Risk \nCreated by:\nSteven Brown\nChristopher Ray\nRyan Robinson\nAndrew Venne - GUI");

		}

	}

	private CardLayout card = new CardLayout();

	private void setUpBody() {
		body.setVisible(true);
		body.setLayout(card);
	}

	/**
	 * @author AJ Venne Created on 8:08:33 PM Nov 26, 2011
	 * 
	 */
	public void changeView(Views v, Object o) {
		if (v == Views.BATTLE) {
			JPanel battleView = new JPanel();

			battleView.setVisible(true);
			body.add(battleView, "Battle");

			card.show(body, "Battle");

			body.getComponent(0).requestFocusInWindow();
		}
		if (v == Views.MAP) {
			JPanel mapView = new JPanel();

			mapView.setVisible(true);
			mapView.add(new MapView(this));
			body.add(mapView, "Map");

			card.show(body, "Map");

			body.getComponent(0).requestFocusInWindow();
		}
		if (v == Views.TITLE) {
			card.show(body, "Title");

			JPanel titleView = new JPanel();
			titleView.setVisible(true);
			titleView.add(new TitleView(this));
			titleView.setLayout(new GridLayout());
			body.add(titleView, "Title");

		}
	}

	/**
	 * @return the game
	 * @author Chris Ray Created on 8:19:12 PM Nov 26, 2011
	 */
	public Game getGameCommander() {
		return game;
	}

}