package roemetz.gui;

import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.*;

import roemetz.core.RoeMetz;

public class RMGUInterface {

	JTextField vR00;
	JTextField vC00;
	JTextField vRC00;
	JTextField vR10;
	JTextField vC10;
	JTextField vRC10;
	JTextField vR01;
	JTextField vC01;
	JTextField vRC01;
	JTextField vR11;
	JTextField vC11;
	JTextField vRC11;
	JTextField vR0;
	JTextField vC0;
	JTextField vRC0;
	JTextField vR1;
	JTextField vC1;
	JTextField vRC1;

	public RMGUInterface(RoeMetz lsttemp, Container cp) {
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		JPanel componentInput = new JPanel();
		componentInput.setLayout(new FlowLayout());
	
		

		vR00 = new JTextField(4);
		vC00 = new JTextField(4);
		vRC00 = new JTextField(4);
		vR10 = new JTextField(4);
		vC10 = new JTextField(4);
		vRC10 = new JTextField(4);
		vR01 = new JTextField(4);
		vC01 = new JTextField(4);
		vRC01 = new JTextField(4);
		vR11 = new JTextField(4);
		vC11 = new JTextField(4);
		vRC11 = new JTextField(4);
		vR0 = new JTextField(4);
		vC0 = new JTextField(4);
		vRC0 = new JTextField(4);
		vR1 = new JTextField(4);
		vC1 = new JTextField(4);
		vRC1 = new JTextField(4);
		
		JLabel vR00Label = new JLabel("vR00: ");
		JLabel vC00Label = new JLabel("vC00: ");
		JLabel vRC00Label = new JLabel("vRC00: ");
		JLabel vR10Label = new JLabel("vR10: ");
		JLabel vC10Label = new JLabel("vC10: ");
		JLabel vRC10Label = new JLabel("vRC10: ");
		JLabel vR01Label = new JLabel("vR01: ");
		JLabel vC01Label = new JLabel("vC01: ");
		JLabel vRC01Label = new JLabel("vRC01: ");
		JLabel vR11Label = new JLabel("vR11: ");
		JLabel vC11Label = new JLabel("vC11: ");
		JLabel vRC11Label = new JLabel("vRC11: ");
		JLabel vR0Label = new JLabel("vR0: ");
		JLabel vC0Label = new JLabel("vC0: ");
		JLabel vRC0Label = new JLabel("vRC0: ");
		JLabel vR1Label = new JLabel("vR1: ");
		JLabel vC1Label = new JLabel("vC1: ");
		JLabel vRC1Label = new JLabel("vRC1: ");
		
		componentInput.add(vR00Label);
		componentInput.add(vR00);
		componentInput.add(vC00Label);
		componentInput.add(vC00);
		componentInput.add(vRC00Label);
		componentInput.add(vRC00);
		componentInput.add(vR10Label);
		componentInput.add(vR10);
		componentInput.add(vC10Label);
		componentInput.add(vC10);
		componentInput.add(vRC10Label);
		componentInput.add(vRC10);
		componentInput.add(vR01Label);
		componentInput.add(vR01);
		componentInput.add(vC01Label);
		componentInput.add(vC01);
		componentInput.add(vRC01Label);
		componentInput.add(vRC01);
		componentInput.add(vR11Label);
		componentInput.add(vR11);
		componentInput.add(vC11Label);
		componentInput.add(vC11);
		componentInput.add(vRC11Label);
		componentInput.add(vRC11);
		componentInput.add(vR0Label);
		componentInput.add(vR0);
		componentInput.add(vC0Label);
		componentInput.add(vC0);
		componentInput.add(vRC0Label);
		componentInput.add(vRC0);
		componentInput.add(vR1Label);
		componentInput.add(vR1);
		componentInput.add(vC1Label);
		componentInput.add(vC1);
		componentInput.add(vRC1Label);
		componentInput.add(vRC1);
		

		cp.add(componentInput);
	}
}
