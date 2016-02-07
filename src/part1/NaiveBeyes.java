package part1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

class Pd{
	int c1;
	int c2;
	Pd(int c1, int c2){
		this.c1=c1;
		this.c2=c2;
	}
}
public class NaiveBeyes {
	HashMap<String,HashMap<String,Pd>> attributes;
	List<List<String>> data;
	
	private void parseInput(String name) {
		try{
			File fin= new File(name);
			FileInputStream fis = new FileInputStream(fin);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			String data="@d";
			String att="@a";
			//parse the line one at a time. break when data is found
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if(line.substring(0,2)==att){
					
				} else if(line.subSequence(0, 2)==data){
					break;
				}
			}
			//parse the remaining data
			while ((line = br.readLine()) != null) {
				
			}
			br.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void main(String args[]){
		NaiveBeyes nb= new NaiveBeyes();
		nb.parseInput("lymph_train.arff");
	}
}
