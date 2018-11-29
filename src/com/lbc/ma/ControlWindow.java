package com.lbc.ma;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ControlWindow implements Runnable {

	int WIDTH = 300;
	int HEIGHT = 200;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		JFrame jf = new JFrame("控制窗口");
		jf.setSize(WIDTH, HEIGHT);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		jf.setContentPane(contentPane);
		JButton addBut = new JButton("添加工作流");
		JButton removeBut = new JButton("移除工作流");
		contentPane.add(addBut);
		contentPane.add(removeBut);

		addBut.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Markov.addWorkflow();
			}
		});

		removeBut.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Markov.randomlyRemoveAWorkflow();
			}
		});
		jf.setVisible(true);
	}

}
