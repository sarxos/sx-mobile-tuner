import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.ToneControl;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.Runnable;

import java.io.IOException;

import sarxos.app.ApplicationRegister;

/**
 * Klasa MIDletu bêd¹cego mobilnym, stroikiem gitarowym. Wystarczy w³¹czyæ i dzia³a.
 * @author Bartosz (SarXos) Firyn
 */
public class SXMobileTuner extends MIDlet implements Runnable, CommandListener {

	private boolean isWorking = false;
	private int position = 0;
	private int startPlaytime = 1000000; 
	private int startVolume = 50;
	private int playtime = startPlaytime;
	private int volume = startVolume;
	private byte podstawa_tonalna = ToneControl.C4; 
	
//	private final byte E4 = C4 + 4;		//  E4 ||--F4--|-Fis4-|--G4--|-Gis4-|--A4--|
//	private final byte H3 = E4 - 5;		//  H3 ||--C4--|-Cis4-|--D4--|-Dis4-|--E4--|
//	private final byte G3 = H3 - 4;		//  G3 ||-Gis3-|--A3--|--B3--|--H3--|------|
//	private final byte D3 = G3 - 5;		//  D3 ||-Dis3-|--E3--|--F3--|-Fis3-|--G3--|
//	private final byte A2 = D3 - 5;		//  A2 ||--B2--|--H2--|--C3--|-Cis3-|--D3--|
//	private final byte E2 = A2 - 5;		//  E2 ||--F2--|-Fis2-|--G2--|-Gis2-|--A2--|
	
	//private Hashtable skale = new Hashtable(5); 
	
	private byte[] skala_podstawowa = new byte[] {4, -1, -5, -10, -15, -20};
	private String[] skala_podstawowa_c = new String[] {"E","H","G","D","A","E"};

	private byte[] skala = skala_podstawowa;
	private String[] skala_c = skala_podstawowa_c;

	
	private class Instrukcja extends Canvas {
		protected void paint(Graphics graph) {
			int xz = 2;
			int yz = 2;
			graph.drawString("Instrukcja:", xz, yz, Graphics.LEFT | Graphics.TOP);
			graph.drawString("Left, Right: Tempo", xz, yz + 14, Graphics.LEFT | Graphics.TOP);
			graph.drawString("Up, Down: Struna", xz, yz + 28, Graphics.LEFT | Graphics.TOP);
			graph.drawString("1, 3: Glosnosc", xz, yz + 42, Graphics.LEFT | Graphics.TOP);
		}
	}
	
	/**
	 * Klasa wyœwietlacza. Do jej zadan nalezy jedynie rysowanie tego co aktualnie 
	 * dzieje sie w aplikcaji.
	 * @author Bartosz (SarXos) Fisyn
	 */
	private class Wyswietlacz extends Canvas {
		
		int width = getWidth();
		int height = getHeight();
		
		int ra = 29;		// od góry ramka
		int s = ra + 8;		// pierwsza struna od góry
		int k = 5;			// skok pomiêdzy strunami
		int kp = 14;		// skok pomiêdzy progami
		
		int w = (6*kp);		// d³ugoœæ strun
		
		protected Image title = null;
		protected Image guitar = null;

		/**
		 * Konstruktor klasy Wyswietlacz. 
		 */
		public Wyswietlacz() {
			try {
				title = Image.createImage("/title.png");
				guitar = Image.createImage("/guitar.png");
			}
			catch(IOException IOex) {}
		}
		
		/* (non-Javadoc)
		 * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
		 */
		public void paint(Graphics graph) {
			graph.setColor(0xFFFFFF);
			graph.fillRect(0, 0, width, height);
			graph.drawImage(title, 1, 2, Graphics.LEFT | Graphics.TOP);
			graph.drawImage(guitar, width-1, height-1, Graphics.BOTTOM | Graphics.RIGHT);
			// Obszary prostok¹tne
			graph.setColor(0x000000);
			//graph.drawRect(0, 0, width - 1, 27);
			graph.drawRect(0, ra, width - 1, (5*k)+(3*k));
			// Guzik
			graph.setColor(0xAAAAAA);
			graph.fillRect(k+(9*kp/2)-(kp/4)+1, s, kp/2, (5*k));
			// Mostek
			graph.setColor(0x996633);
			graph.drawLine(k-2, s, k-2, s+(5*k));	// Mostek
			graph.drawLine(k-1, s, k-1, s+(5*k));
			// Progi
			graph.drawLine(k+(1*kp), s, k+(1*kp), s+(5*k));	// progi I
			graph.drawLine(k+(2*kp), s, k+(2*kp), s+(5*k));	//		 II
			graph.drawLine(k+(3*kp), s, k+(3*kp), s+(5*k));	//		 III
			graph.drawLine(k+(4*kp), s, k+(4*kp), s+(5*k));	//		 IV
			graph.drawLine(k+(5*kp), s, k+(5*kp), s+(5*k));	//		 V
			// Struny
			graph.drawLine(k, s+(0*k), k+w, s+(0*k));	// 1 struna
			graph.drawLine(k, s+(1*k), k+w, s+(1*k));	// 2 struna
			graph.drawLine(k, s+(2*k), k+w, s+(2*k));	// 3 struna
			graph.drawLine(k, s+(3*k), k+w, s+(3*k));	// 4 struna
			graph.drawLine(k, s+(4*k), k+w, s+(4*k));	// 5 struna
			graph.drawLine(k, s+(5*k), k+w, s+(5*k));	// 6 struna
			// Pogrubiona struna strojona
			graph.setColor(0x000077);
			graph.drawLine(k, s+(position*k), k+w, s + (position*k));
			graph.drawLine(k, s+(position*k)+1, k+w, s + (position*k)+1);
			// Literka z numerem obok strojonej struny
			graph.setColor(0x000000);
			int r = graph.getFont().getHeight() / 2;
			graph.drawString(skala_c[position] + (position+1), k+w+2, s-r+(position*k), Graphics.LEFT | Graphics.TOP);
			// Informacje tekstowe
			String stempo = (new Integer(5000000/playtime)).toString();
			graph.drawString("Tempo: " + stempo, 2, s+(6*k)+4, Graphics.LEFT | Graphics.TOP);
			graph.drawString("Skala: podst., ton E",2 , s+(6*k)+14, Graphics.LEFT | Graphics.TOP);
		}
		
		/* (non-Javadoc)
		 * @see javax.microedition.lcdui.Canvas#keyPressed(int)
		 */
		protected void keyPressed(int keyCode) {
			switch(getGameAction(keyCode)) {
				case Canvas.DOWN:
					if(position + 1 == skala.length) {
						position = 0;
					} else {
						position++;
					}
					break;
				case Canvas.UP:
					if(position == 0) {
						position = skala.length - 1;
					} else {
						position--;
					}
					break;
				case Canvas.LEFT:
					if(playtime/1000 > 250) {
						playtime /= 2;
						ApplicationRegister.getInstance().setValue("app.playtime", playtime);
					}
					break;
				case Canvas.RIGHT:
					if(playtime/1000 < 4000) {
						playtime *= 2;
						ApplicationRegister.getInstance().setValue("app.playtime", playtime);
					}
					break;
			}
			switch(keyCode) {
				case Canvas.KEY_NUM1:
					if(volume > 20) {
						volume -= 10;
						ApplicationRegister.getInstance().setValue("app.volume", volume);
					}
					break;
				case Canvas.KEY_NUM3:
					if(volume < 100) {
						volume += 10;
						ApplicationRegister.getInstance().setValue("app.volume", volume);
					}
					break;
			}
			repaint();
		}
	}

	private Thread watek = new Thread(this);
	private Display ekran = Display.getDisplay(this);
	private Wyswietlacz forma = new Wyswietlacz();
	private Instrukcja instr = new Instrukcja();
	private final Command CMD_EXIT = new Command("Koniec", Command.EXIT, 0); 
	private final Command CMD_START = new Command("Start", Command.SCREEN, 0); 
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			isWorking = true;
			do {
				int gra = (playtime - (playtime / 10)) / 1000;
				int pauza = playtime / 1000;
				Manager.playTone(podstawa_tonalna + skala[position], gra, volume);
				Thread.sleep(pauza);
			} while(isWorking);
		} 
		catch(MediaException Mex) {}
		catch(InterruptedException Iex) {}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable disp) {
		if(cmd == CMD_EXIT) {
			try {
				isWorking = false;	// nie pracuje
				watek.join();		// zatrzymanie w¹tku
				destroyApp(true);	// niszczenie aplikacji
				notifyDestroyed();	// informujemy o zniszczeniu srodowisko 
			}
			catch(InterruptedException Iex) {}
			catch(MIDletStateChangeException MSCex) {}
		}
		if(cmd == CMD_START) {
			ApplicationRegister appReg = ApplicationRegister.getInstance(); 
			try {
				// probujemy pobrac z rejestru volume
				volume = appReg.getValue("app.volume");
			}
			catch(Exception Ex) {
				// jesli nie da sie pobrac to ustawiamy wartosc startowa volume
				volume = startVolume;
			}
			try {
				// probujemy pobrac z rejestru tempo
				playtime = appReg.getValue("app.playtime");
			}
			catch(Exception Ex) {
				// jesli nie udalo sie pobrac to ustawiamy tempo startowe
				playtime = startPlaytime;
			}
			// wyswietlanmie
			ekran.setCurrent(forma);
			watek.start();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	public void startApp() throws MIDletStateChangeException {

		forma.addCommand(CMD_EXIT);		// dodajemy polecenie 'Exit'
		instr.addCommand(CMD_START);	// dodajemy polecenie 'Start'
		
		forma.setCommandListener(this);	// ustawiamy listenery polecen
		instr.setCommandListener(this);
		
		ekran.setCurrent(instr);
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	public void pauseApp() {}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		if(isWorking) {
			commandAction(CMD_EXIT, null);
		}
	}
}
