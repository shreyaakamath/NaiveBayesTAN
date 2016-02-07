package part1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Pd{
	float c1;
	int index;
	float c2;
	
	Pd(){
		c1=0;
		c2=0;
	}
	Pd(int i){
		this.index=i;
	}
	
	public float getC1() {
		return c1;
	}
	public void setC1(float c1) {
		this.c1 = c1;
	}
	public float getC2() {
		return c2;
	}
	public void setC2(float c2) {
		this.c2 = c2;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
public class NaiveBeyes {
	HashMap<String,HashMap<String,Pd>> attributes;
	List<List<String>> data;
	String[] classArr;
	int[] classCnt;
	int totalAttNo;
	int totalData;
	
	public int getTotalData() {
		return totalData;
	}

	public void setTotalData(int totalData) {
		this.totalData = totalData;
	}

	public void setTotalAttNo(int totalAttNo) {
		this.totalAttNo = totalAttNo;
	}

	NaiveBeyes(){
		attributes = new HashMap<String,HashMap<String,Pd>>();
		data = new ArrayList<List<String>>();
		classArr = new String[2];
		classCnt= new int[2];
	}
	
	/**
	 * method that parses the input and stored it in the internal DS
	 * @param name
	 */
	private void parseInput(String name) {
		try{
			File fin= new File(name);
			FileInputStream fis = new FileInputStream(fin);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			String dataPattern="@d";
			String attPattern="@a";
			String classPattern="class";
			int i=-1;
			//parse the line one at a time. break when data is found. put the attributes in HashMap<String,HashMap<String,Pd>> attributes
			while ((line = br.readLine()) != null) {
				//System.out.println(line.substring(0,2));
				if(line.substring(0,2).equalsIgnoreCase(attPattern)){
					  i++;
					  //regex's , pattern and matcher objects to get the feature name and the attributes of the feature
					  String featRegex = "\'([^\']*)\'";
				      String attrRegex="\\{(.*?)\\}";
				      Pattern p1 = Pattern.compile(featRegex);
				      Pattern p2 = Pattern.compile(attrRegex);
				      Matcher m1 = p1.matcher(line);
				      Matcher m2 = p2.matcher(line);
				      m1.find();m2.find();
				      String feature = m1.group(1);
				      String att=m2.group(1);
				      
				      //if the feature is class then populate the class array else populate the inner hashmap with attribute to Pd objects
				      
				      if(feature.equals(classPattern)){
				    	  classArr[0]=att.split(",")[0];
				    	  classArr[1]=att.split(",")[1];
				      }else{
				    	  HashMap<String,Pd> inner = new HashMap<String,Pd>();
					      for(String str:att.split(",")){
						      inner.put(str,new Pd(i));
					      }
					      attributes.put(feature,inner);
				      }
				      
				} else if(line.substring(0, 2).equalsIgnoreCase(dataPattern)){
					break;
				}
			}
			//parse the remaining data and put it into List<List> data
			while ((line = br.readLine()) != null) {
				List<String> inner = new ArrayList<String>();
				for(String str:line.split(",")){
					inner.add(str);
				}
				data.add(inner);
			}
			
			//since attributes hashmap is full , populate the number_of_attributes variable. Populate the total number of data instances
			setTotalAttNo(attributes.size());
			setTotalData(data.size());
			br.close();
			
			//call this method to calculate the numbers for each class vars
			noClassVars();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * method that prints the attribute hashmap for verification
	 */
	void verify(){
		System.out.println("-----------Attributes hashmap-------");
		for(Map.Entry<String,HashMap<String,Pd>> entry:attributes.entrySet()){
			System.out.println(entry.getKey()+"->");
			for(Map.Entry<String,Pd> inner: entry.getValue().entrySet()){
				System.out.print(inner.getKey()+" ->");
				System.out.println("p1="+inner.getValue().getC1()+","+"p2="+inner.getValue().getC2()+"index="+inner.getValue().getIndex());
				System.out.println();
			}
			System.out.println();
		}
		
		System.out.println("class values --- "+classArr[0]+","+classArr[1]);
		System.out.println("total att ----"+totalAttNo);
	}
 
	/**
	 * laplace equivalence = (number of times attribute a and class variable b occur +1)/(total number of attributes possible for this feature + number of times this feature occurs)
	 * @param jointCnt
	 * @param typesCnt
	 * @param classCnt
	 * @return
	 */
	float laplace(int jointCnt,int typesCnt,int classCnt){
		float ret=(float)(jointCnt+1)/(typesCnt+classCnt);
		return ret;
	}
	
	/**
	 * calculate and store the class feature probabilities
	 */
	void noClassVars(){
		int c1=0;int c2=0;
		for(List<String> rowData:data){
			if(rowData.get(totalAttNo).trim().equalsIgnoreCase(classArr[0].trim())) c1++;
			if(rowData.get(totalAttNo).trim().equalsIgnoreCase(classArr[1].trim())) c2++;
		}
		classCnt[0]=c1;
		classCnt[1]=c2;
		
	}
	
	/**
	 * computer the probability distribution and store it in the attributes hashmap
	 */
	void computePd(){
		for(Map.Entry<String,HashMap<String,Pd>> allAttr:attributes.entrySet()){
			int count1=0;int count2=0;float pd1;float pd2;
			int typesCnt=allAttr.getValue().size();//number of attributed for this one feature
			for(Map.Entry<String,Pd> allFeat: allAttr.getValue().entrySet()){
				String feature = allFeat.getKey();//wrong name this is attr not feature
				Pd obj=allFeat.getValue();
				int i=obj.getIndex();
				for(List<String> rowData:data){
					String rowi=rowData.get(i).trim();
					String rowEnd=rowData.get(totalAttNo).trim();
					String c1=classArr[0].trim();
					String c2=classArr[1].trim();
					
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c1.trim())) {
						count1++;
					}
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c2.trim())) {
						count2++;
					}
				}
				pd1=laplace(count1,typesCnt,classCnt[0]);
				pd2=laplace(count2,typesCnt,classCnt[1]);
				obj.setC1(pd1);
				obj.setC2(pd2);
			}
		}
	}
	
	
	public static void main(String args[]){
		NaiveBeyes nb= new NaiveBeyes();
		nb.parseInput("lymph_train.arff");
		nb.computePd();
		nb.verify();
		System.out.println();
		
		
	}
}
