package part1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Pd{
//	float c1;
	double c1;
	int index;
//	float c2;
	double c2;
	
	Pd(){
		c1=0;
		c2=0;
	}
	Pd(int i){
		this.index=i;
	}
	
	public double getC1() {
		return c1;
	}
	public void setC1(double c1) {
		this.c1 = c1;
	}
	public double getC2() {
		return c2;
	}
	public void setC2(double c2) {
		this.c2 = c2;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
class SampleData{
	LinkedHashMap<String,LinkedHashMap<String,Pd>> attributes;
	List<List<String>> data;
	List<String> featNames;
	String[] classArr;
	int[] classCnt;
	double[] classProb;
	int totalAttNo;
	int totalData;
	List<List<Double>> adjList;
	
	SampleData(){
		attributes = new LinkedHashMap<String,LinkedHashMap<String,Pd>>();
		data = new ArrayList<List<String>>();
		classArr = new String[2];
		classCnt= new int[2];
		featNames= new ArrayList<String>();
		classProb = new double[2];
		adjList= new ArrayList<List<Double>>();
	}
	
	public int getTotalAttNo() {
		return totalAttNo;
	}
	public void setTotalAttNo(int totalAttNo) {
		this.totalAttNo = totalAttNo;
	}
	public int getTotalData() {
		return totalData;
	}
	public void setTotalData(int totalData) {
		this.totalData = totalData;
	}
}

public class NaiveBeyes {
	SampleData train;
	SampleData test;
	

	NaiveBeyes(){
		train=new SampleData();
		test=new SampleData();
	}
	
	/**
	 * method that parses the input and stored it in the internal DS
	 * @param name
	 */
	private void parseInput(String name,SampleData obj) {
		try{
			File fin= new File(name);
			FileInputStream fis = new FileInputStream(fin);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			String dataPattern="@d";
			String attPattern="@a";
			String classPattern="class";
			int i=-1;
			//parse the line one at a time. break when data is found. put the attributes in LinkedHashMap<String,LinkedHashMap<String,Pd>> attributes
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
				      
				      //if the feature is class then populate the class array else populate the inner LinkedHashMap with attribute to Pd objects
				      
				      if(feature.equals(classPattern)){
				    	  obj.classArr[0]=att.split(",")[0].trim();
				    	  obj.classArr[1]=att.split(",")[1].trim();
				      }else{
				    	  LinkedHashMap<String,Pd> inner = new LinkedHashMap<String,Pd>();
					      for(String str:att.split(",")){
						      inner.put(str.trim(),new Pd(i));
					      }
					      obj.attributes.put(feature,inner);
					      obj.featNames.add(feature);
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
				obj.data.add(inner);
			}
			
			//since attributes LinkedHashMap is full , populate the number_of_attributes variable. Populate the total number of data instances
			obj.setTotalAttNo(obj.attributes.size());
			obj.setTotalData(obj.data.size());
			br.close();
			
			//call this method to calculate the numbers for each class vars
			noClassVars();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * method that prints the attribute LinkedHashMap for verification
	 */
	void verify(SampleData obj){
		System.out.println("-----------Attributes LinkedHashMap-------");
		for(Map.Entry<String,LinkedHashMap<String,Pd>> entry:obj.attributes.entrySet()){
			System.out.println(entry.getKey()+"->");
			for(Map.Entry<String,Pd> inner: entry.getValue().entrySet()){
				System.out.print(inner.getKey()+" ->");
				System.out.println("p1="+inner.getValue().getC1()+","+"p2="+inner.getValue().getC2()+"index="+inner.getValue().getIndex());
				System.out.println();
			}
			System.out.println();
		}
		
		System.out.println("class values --- "+obj.classArr[0]+","+obj.classArr[1]);
		System.out.println("total att ----"+obj.totalAttNo);
	}
 
	/**
	 * laplace equivalence = (number of times attribute a and class variable b occur +1)/(total number of attributes possible for this feature + number of times this feature occurs)
	 * @param jointCnt
	 * @param typesCnt
	 * @param classCnt
	 * @return
	 */
	double laplace(int jointCnt,int typesCnt,int classCnt){
		double ret=(float)(jointCnt+1)/(typesCnt+classCnt);
		return ret;
	}
	
	/**
	 * calculate and store the class feature probabilities
	 */
	void noClassVars(){
		int c1=0;int c2=0;
		for(List<String> rowData:train.data){
			if(rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[0].trim())) c1++;
			if(rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[1].trim())) c2++;
		}
		train.classCnt[0]=c1;
		train.classCnt[1]=c2;
		train.classProb[0]=laplace(c1,train.totalData,2);
		train.classProb[1]=laplace(c2,train.totalData,2);
		
	}
	
	/**
	 * computer the probability distribution and store it in the attributes LinkedHashMap
	 */
	void computePd(){
		for(Map.Entry<String,LinkedHashMap<String,Pd>> allAttr:train.attributes.entrySet()){
			double pd1;double pd2;
			int typesCnt=allAttr.getValue().size();//number of attributed for this one feature
			for(Map.Entry<String,Pd> allFeat: allAttr.getValue().entrySet()){
				int count1=0;int count2=0;
				String feature = allFeat.getKey();//wrong name this is attr not feature
				Pd obj=allFeat.getValue();
				int i=obj.getIndex();
				for(List<String> rowData:train.data){
					String rowi=rowData.get(i).trim();
					String rowEnd=rowData.get(train.totalAttNo).trim();
					String c1=train.classArr[0].trim();
					String c2=train.classArr[1].trim();
					if(feature.trim().equalsIgnoreCase("oval")){
//					System.out.println(rowi+"=="+feature+" -> "+rowi.trim().equalsIgnoreCase(feature.trim())+" "+rowEnd+" == "+c1+" ->"+rowEnd.trim().equalsIgnoreCase(c1.trim()));
//					System.out.println(rowi+"=="+feature+" -> "+rowi.trim().equalsIgnoreCase(feature.trim())+" "+rowEnd+" == "+c2+" ->"+rowEnd.trim().equalsIgnoreCase(c2.trim()));
					}
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c1.trim())) {
						count1++;
						if(feature.trim().equalsIgnoreCase("oval")){
							System.out.println("count="+count1);
							System.out.println(rowi+"=="+feature+" -> "+rowi.trim().equalsIgnoreCase(feature.trim())+" "+rowEnd+" == "+c1+" ->"+rowEnd.trim().equalsIgnoreCase(c1.trim()));
						}
					}
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c2.trim())) {
						count2++;
					}
				}
				System.out.println(feature+" - "+"count1= "+count1+" count2="+count2);
				pd1=laplace(count1,typesCnt,train.classCnt[0]);
				pd2=laplace(count2,typesCnt,train.classCnt[1]);
				System.out.println();
				obj.setC1(pd1);
				obj.setC2(pd2);
			}
		}
	}
	
	/**
	 * predict the class for the test data based on training data for naive bayes
	 */
	void predictClass(){
		double prod1=1;double prod2=1;
		for(int i=0;i<train.totalAttNo;i++){
			System.out.println(train.featNames.get(i)+" "+"class");
		}
		System.out.println();
		int count=0;
		for(List<String> rowData:test.data){
			prod1=train.classProb[0];prod2=train.classProb[1];
			String op=null;
			for(int i=0;i<rowData.size()-1;i++){
				String val = rowData.get(i).trim();
				String feature = train.featNames.get(i);
				LinkedHashMap<String,Pd> hm = train.attributes.get(feature);
				Pd obj=hm.get(val);
				prod1*=obj.getC1();
				prod2*=obj.getC2();
			}
			if(Double.compare(prod1,prod2)>=0){
				op=test.classArr[0];
			}else if(Double.compare(prod1,prod2)<0){
				op=test.classArr[1];
			}
			if(op.trim().equalsIgnoreCase(rowData.get(train.totalAttNo).trim())) count++;
			System.out.println(op+" "+rowData.get(train.totalAttNo));
		}
		System.out.println();
		System.out.println(count);
		
	}
	
	void computeWeight(){
		for(Map.Entry<String,LinkedHashMap<String,Pd>> outer: train.attributes.entrySet()){
			String key1=outer.getKey();//lymphatics
			LinkedHashMap<String,Pd> value1=outer.getValue();//{ normal, arched, deformed, displaced}
			List<Double> temp_list = new ArrayList<Double>();
			int xi_len=value1.size();
			for(Map.Entry<String,LinkedHashMap<String,Pd>> inner: train.attributes.entrySet()){
				double sum=0;
				String key2=inner.getKey();// block_of_affere
				LinkedHashMap<String,Pd> value2=inner.getValue();//{ no, yes}
				int xj_len=value2.size();
				if(key1.trim().equalsIgnoreCase(key2.trim())){temp_list.add(new Double(-1));continue;}
				//now iterate over  each of the attributes of outer and inner feature
				for(Map.Entry<String, Pd> outer1:value1.entrySet()){//normal
					double xi_y=outer1.getValue().getC1();
					double xi_y_dash=outer1.getValue().getC2();
					int i=outer1.getValue().getIndex();
					String val1=outer1.getKey();
					for(Map.Entry<String,Pd> inner1:value2.entrySet()){//{no , yes}
						double xj_y=inner1.getValue().getC1();
						double xj_y_dash=inner1.getValue().getC2();
						int j=inner1.getValue().getIndex();
						String val2=inner1.getKey();
						double numerator1;double denominator1;double multiplicant1;
						double numerator2;double denominator2;double multiplicant2;
						denominator1=xi_y*xj_y;
						denominator2=xi_y_dash*xj_y_dash;
						int count_xi_xj_y=0;int count_xi_xj_y_dash=0;
						//get xi,xj,y count values
						for(List<String> rowData:train.data){
							if(rowData.get(i).trim().equalsIgnoreCase(val1.trim()) &&
									rowData.get(j).trim().equalsIgnoreCase(val2.trim()) &&
									rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[0].trim())) {
								count_xi_xj_y++;
							}
							if(rowData.get(i).trim().equalsIgnoreCase(val1.trim()) &&
									rowData.get(j).trim().equalsIgnoreCase(val2.trim()) &&
									rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[1].trim())) {
								count_xi_xj_y_dash++;
							}
						}
						numerator1=laplace(count_xi_xj_y, xi_len*xj_len, train.classCnt[0]);
						numerator2=laplace(count_xi_xj_y_dash, xi_len*xj_len, train.classCnt[1]);
						multiplicant1=laplace(count_xi_xj_y, xi_len*xj_len*2, train.totalData);
						multiplicant2=laplace(count_xi_xj_y_dash, xi_len*xj_len*2, train.totalData);
						double weight1=(multiplicant1*(Math.log(numerator1/denominator1)/Math.log(2)));
						double weight2=multiplicant2*(Math.log(numerator2/denominator2)/Math.log(2));
						sum+=weight1+weight2;
					}
				}
				temp_list.add(sum);	
			}
			train.adjList.add(temp_list);
		}
	}
	void printWeight(){
		for(List<Double> list : train.adjList){
			for(Double d: list) System.out.print(d+" ");
			System.out.println();
		}
	}
	public static void main(String args[]){
		NaiveBeyes nb= new NaiveBeyes();
		
		nb.parseInput("lymph_train.arff",nb.train);
		nb.computePd();
		nb.computeWeight();
		nb.printWeight();
//		nb.verify(nb.train);
//		nb.parseInput("lymph_test.arff",nb.test);
//		nb.verify(nb.train);
//		System.out.println();
//		nb.predictClass();
		
	}
}
