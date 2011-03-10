package sarxos.app;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Klasa ApplicationRegister jak sama nazwa wskazuje sluzy do przechowywania 
 * roznych wartosci (boolean, byte, int, long) w pamieci nieulotnej. Kazda
 * zapisywana wartosc ma stowazyszony klucz, ktory mozemy dowolnie sobie
 * wymyslic. Dla tegho jednego konkretnego klucza bedzie istniala tylko jedna
 * wartosc do zwrocenia.
 * @author Bartosz (SarXos) Firyn
 */
public class ApplicationRegister implements RecordListener {
	
	private static ApplicationRegister appRegInstance = null;
	private static RecordStore appRegRMS = null;
	private static String appRegName = "ApplicationRegister";
	
	/**
	 * Konstruktor klasy ApplicationRegister. Poniewa¿ klasa ta jest singletonem, to
	 * jej konstruktor jest prywany. Dostêp do obiektu tej klasy uzyskujemy dziêki
	 * metodzie getInstance(). Oto przyklad:
	 * {code}
	 * ApplicationRegister appReg = ApplicationRegister.getInstance();
	 * int mojaWartosc = 10;
	 * appReg.setValue("moj.klucz", mojaWartosc);
	 * {/code}
	 * W tym przypadku wartosc MojaWartosc zostanie na stale zapisana w pamieci
	 * nieulotnej urzadzenia. Aby ja usunac nalezy zastosowac metode delValue().
	 */
	private ApplicationRegister() {
		try {
			appRegRMS = RecordStore.openRecordStore(appRegName, true);
			appRegRMS.addRecordListener(this);
		}
		catch(RecordStoreFullException RSFex) {}
		catch(RecordStoreNotFoundException RSNFex) {}
		catch(RecordStoreException RSex) {}
	}
	
	/**
	 * Pobieranie egzemplarza singletonu.
	 * @return ApplicationRegister
	 */
	public static ApplicationRegister getInstance() {
		if(appRegInstance == null) {
			appRegInstance = new ApplicationRegister();
		}
		return appRegInstance; 
	}

	/**
	 * Wyszukiwanie rekordow, ktorych klucze maja konkretna podana wartosc. Jednak
	 * w przypadku tej klasy, dla konkretnego klucza moze istniec tylko jedna
	 * wartosc zapisana w pamieci. 
	 * @param rms Magazyn typu RecordStore w ktorym szukamy
	 * @param key Klucz dla jakiego szukamy rekordu
	 * @return RecordEnumeration
	 */
	private RecordEnumeration find(RecordStore rms, String key) {
		final String keyCompared = key;
		RecordFilter filter = new RecordFilter() {
			public boolean matches(byte[] data) {
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(data);
					DataInputStream dis = new DataInputStream(bais);
					String keyToCompare = dis.readUTF();
					if(keyCompared.compareTo(keyToCompare) == 0) {
						return true;
					} else {
						return false; 
					}
				}
				catch(IOException IOex) {
					return false;
				}
			}
		};
		try {
			return rms.enumerateRecords(filter, null, true);
		}
		catch(RecordStoreException RSex) {
			return null;
		}
	}
	
	/**
	 * Wewnetrzna metoda zapisujaca tylko wartosci int w pamieci. Kazda z
	 * metod zapisujacych na koncu wywoluje wlasnie ta metode.
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param num Zapisywana wartosc
	 */
	private void set(String key, int num) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeUTF(key);
			dos.writeInt(num);
			dos.close();
		}
		catch(IOException IOex) {}
		byte[] data = baos.toByteArray();
		RecordEnumeration re = find(appRegRMS, key);
		try {
			if(re.numRecords() > 0) {
				appRegRMS.setRecord(re.nextRecordId(), data, 0, data.length);
			} else {
				appRegRMS.addRecord(data, 0, data.length);
			}
		}
		catch(RecordStoreException RSex) {};
	}
	
	/**
	 * Zapisywanie wartoœci typu int w pamieci.
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param num Zapisywana wartosc
	 */
	public void setValue(String key, int num) {
		set(key, num);
	}
	
	/**
	 * Zapisywanie wartoœci typu boolean w pamieci.
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param value Zapisywana wartosc typu boolean
	 */
	public void setValue(String key, boolean value) {
		int a = 0;
		if(value == true) a = 1;
		set(key, a);
	}
	
	/**
	 * Zapisywanie wartoœci typu byte w pamieci. Jako argumenty podajemy klucz key
	 * w postaci lancucha znakow (np. "app.version") oraz liczbe byte. Liczba ta
	 * zostanie zapisana w pamieci nieulotnej i w dowolnym momencie w kodzie
	 * po konstrukcji singletonu moze zostac pobrana i uzyta. 
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param num Zapisywana wartosc
	 */
	public void setValue(String key, byte num) {
		set(key, (int)num);
	}
	
	/**
	 * Zapisywanie wartoœci typu long w pamieci.
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param num Zapisywana wartosc
	 */
	public void setValue(String key, long num) {
		set(key, (int)num);
	}

	/**
	 * Zapisywanie wartoœci typu long w pamieci.
	 * @param key Klucz pod jakim zapisana bedzie wartosc
	 * @param data Zapisywana wartosc w postaci tablicy byte[]
	 */
	public void setValue(String key, byte[] data) {
		RecordEnumeration re = find(appRegRMS, key);
		try {
			if(re.numRecords() > 0) {
				appRegRMS.setRecord(re.nextRecordId(), data, 0, data.length);
			} else {
				appRegRMS.addRecord(data, 0, data.length);
			}
		}
		catch(RecordStoreException RSex) {};
	}
	
	/**
	 * Pobieranie wartosci dla zadanego klucza. Dla jednego klucza istnieje 
	 * tylko jedna wartosc zwracana (typu int). 
	 * @param key Klucz spod jakiego pobieramy wartosc
	 * @return int Pobrana wartosc spod danego klucza
	 */
	public int getValue(String key) throws Exception {
		int num = -1;
		int ret = 0;
		try {
			RecordEnumeration re = find(appRegRMS, key);
			if((num = re.numRecords()) > 0) {
				byte[] data = re.nextRecord();
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
				dis.readUTF();
				ret = dis.readInt();
				dis.close();
			}
		}
		catch(RecordStoreException RSex) {}
		catch(IOException IOex) {}
		if(num == -1) {
			throw new Exception("W magazynie RMS nie znaleziono rekordu o takim kluczu (" + key + ").");
		}
		return ret;
	}

	public byte[] getValueAsByte(String key) throws Exception {
		byte[] data = null;
		try {
			RecordEnumeration re = find(appRegRMS, key);
			if(re.numRecords() > 0) {
				data = re.nextRecord();
			}
		}
		catch(RecordStoreException RSex) {}
		if(data == null) {
			throw new Exception("W magazynie RMS nie znaleziono rekordu o takim kluczu (" + key + ").");
		}
		return data;
	}
	
	/**
	 * Usuwanie konkretnego rekordu z magazynu.
	 * @param key Klucz rekordu jaki usuwamy
	 */
	public void delValue(String key) throws Exception {
		RecordEnumeration re = find(appRegRMS, key);
		if(re.numRecords() > 0) {
			try {
				appRegRMS.deleteRecord(re.nextRecordId());
			}
			catch(RecordStoreException RSex) {}
		} else {
			throw new Exception(
				"Z magazynu RMS nie mozna usunac wartosci poniewaz nie znaleziono rekordu o takim kluczu (" +
				key + ").");
		}
	}
	
	/**
	 * Usuwanie calego magazynu rekordow. Przydaje sie gdy chcemy usunac calkowicie
	 * dany magazyn, zamiast kasowac jego poszczegolne rekordy.
	 */
	public void clear() {
		try {
			appRegRMS.closeRecordStore();
			RecordStore.deleteRecordStore(appRegName);
		}
		catch(RecordStoreException RSex) {}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordListener#recordAdded(javax.microedition.rms.RecordStore, int)
	 */
	public void recordAdded(RecordStore rs, int id) {
		try {
			System.out.println(
					"::: RMS: Dodano nowy rekord id=" + id + 
					" w magazynie " + rs.getName()
			);
		}
		catch(RecordStoreNotOpenException RSNOex) {}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordListener#recordChanged(javax.microedition.rms.RecordStore, int)
	 */
	public void recordChanged(RecordStore rs, int id) {
		try {
			System.out.println(
					"::: RMS: Zmieniono rekord id=" + id + 
					" w magazynie " + rs.getName()
			);
		}
		catch(RecordStoreNotOpenException RSNOex) {}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordListener#recordDeleted(javax.microedition.rms.RecordStore, int)
	 */
	public void recordDeleted(RecordStore rs, int id) {
		try {
			System.out.println(
					"::: RMS: Usunieto rekord id=" + id + 
					" w magazynie " + rs.getName()
			);
		}
		catch(RecordStoreNotOpenException RSNOex) {}
	}
}
