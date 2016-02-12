package part1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

class PrimHelper{
	Edge chosenEdge;
	int chosenVertex;
	double weight;
	PrimHelper(Edge e, int v,double w){
		this.chosenEdge=e;
		this.chosenVertex=v;
		this.weight=w;
	}
}

class PrimComparator implements Comparator<PrimHelper>{
	public int compare(PrimHelper obj1, PrimHelper obj2){
		if(obj1.weight<obj2.weight) return 1;
		else if (obj1.weight>obj2.weight) return -1;
		else {
			if(obj1.chosenVertex<obj2.chosenVertex) return -1;
			else if (obj1.chosenVertex>obj2.chosenVertex) return 1;
			else return 0;
		}
	}
}
class Edge{
	int v ;
	int u;
	double weight;
	Edge(int v, int u, double weight){
		this.v=v;//parent
		this.u=u;//child
		this.weight=weight;
	}
}
class TanMeta{
	double c1;
	double c2;
	TanMeta(double c1, double c2){
		this.c1=c1;this.c2=c2;
	}
}
class Pd{
	double c1;
	double c2;
	int index;
	LinkedHashMap<String,TanMeta> hm;

	
	Pd(){
		c1=0;
		c2=0;
		hm=new LinkedHashMap<String,TanMeta>();
	}
	Pd(int i){
		this.index=i;
		hm=new LinkedHashMap<String,TanMeta>();
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
	List<Edge> mst;
	LinkedHashMap<Integer,Integer> parent;
	
	SampleData(){
		attributes = new LinkedHashMap<String,LinkedHashMap<String,Pd>>();
		data = new ArrayList<List<String>>();
		classArr = new String[2];
		classCnt= new int[2];
		featNames= new ArrayList<String>();
		classProb = new double[2];
		adjList= new ArrayList<List<Double>>();
		mst=new ArrayList<Edge>();
		parent = new LinkedHashMap<Integer,Integer>();
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
	void parseInput(String name,SampleData obj){
		try {
		BufferedReader reader = new BufferedReader(new FileReader(name));
		Instances data = new Instances(reader);
		String classPattern="class";
		reader.close();
		data.setClassIndex(data.numAttributes() - 1);
		//read in attributes and values
		for(int i=0;i<data.numAttributes();i++){
			Attribute attr = data.attribute(i);
			String allAttr=attr.toString();
			
			String feature=attr.name();
			int num=attr.numValues();
			String attrRegex="\\{(.*?)\\}";
			Pattern p2 = Pattern.compile(attrRegex);
			Matcher m2 = p2.matcher(allAttr);
		    m2.find();
		    String att=m2.group(1);
		    String[] each = att.split(",");
		    
		     if(feature.trim().equals(classPattern.trim())){
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
		}
		
		// put this rowData into train list
		for (int i = 0;i < data.numInstances();i++){
			Instance inst = data.instance(i);
			List<String> inner = new ArrayList<String>();
			for(String str:inst.toString().split(",")){
				inner.add(str);
			}
			obj.data.add(inner);
			
		}
		
		obj.setTotalAttNo(obj.attributes.size());
		obj.setTotalData(obj.data.size());
		//call this method to calculate the numbers for each class vars
		noClassVars();
		} catch (IOException e) {
			e.printStackTrace();
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
		
		System.out.println("------Adjancency matrix-----");
		for(List<Double> list : train.adjList){
			for(Double d: list) System.out.print(d+" ");
			System.out.println();
		}
		
		System.out.println("-----MST----");
		for(Edge e:train.mst){
			System.out.println("("+e.v+","+e.u+")");
		}
		
		System.out.println("------Parent hash child-> parent ----");
		for(Map.Entry<Integer,Integer> entry:train.parent.entrySet()){
			System.out.println(entry.getKey()+"---"+entry.getValue());
		}
		
		System.out.println("TAN cpd");
		for(Map.Entry<String,LinkedHashMap<String,Pd>> entry:train.attributes.entrySet()){
			for(Map.Entry<String,Pd> map:entry.getValue().entrySet()){
				System.out.print(map.getKey()+"--->");
				LinkedHashMap<String, TanMeta> hm = map.getValue().hm;
				for(Map.Entry<String,TanMeta> inner:hm.entrySet()){
					System.out.print(inner.getKey()+"---"+inner.getValue().c1+" ,"+inner.getValue().c2);
				}
				System.out.println();
			}
			System.out.println("==================");
		}
	}
 
	/**
	 * laplace equivalence = (number of times attribute a and class variable b occur +1)/(total number of attributes possible for this feature + number of times this feature occurs)
	 * @param jointCnt
	 * @param typesCnt
	 * @param classCnt
	 * @return
	 */
	double laplace(int jointCnt,int typesCnt,int classCnt){
		double ret=(double)(jointCnt+1)/(typesCnt+classCnt);
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
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c1.trim())) {
						count1++;
					}
					if(rowi.trim().equalsIgnoreCase(feature.trim())&& rowEnd.trim().equalsIgnoreCase(c2.trim())) {
						count2++;
					}
				}
//				System.out.println(feature+" - "+"count1= "+count1+" count2="+count2);
				pd1=laplace(count1,typesCnt,train.classCnt[0]);
				pd2=laplace(count2,typesCnt,train.classCnt[1]);
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
			double posterior=prod1+prod2;
			double ans1=prod1/posterior;
			double ans2=prod2/posterior;
			double finalans=0;
			NumberFormat formatter = new DecimalFormat("#0.000000000000");
			if(Double.compare(prod1,prod2)>=0){
				op=test.classArr[0];
				finalans=ans1;
			}else if(Double.compare(prod1,prod2)<0){
				op=test.classArr[1];
				finalans=ans2;
			}
			
			if(op.trim().equalsIgnoreCase(rowData.get(train.totalAttNo).trim())) count++;
			System.out.print(op+" "+rowData.get(train.totalAttNo)+" ");
			System.out.println(formatter.format(finalans));
		}
		System.out.println();
		System.out.println(count);
		
	}
	/**
	 * TAN compute weight between edges
	 */
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
	
	
	/**
	 * compute maximal spanning tree using Prim
	 */
	void primMst(){
		List<Integer> vnew= new ArrayList<Integer>();
		List<Integer> allV= new ArrayList<Integer>();
		for(int i=0;i<train.totalAttNo;i++) allV.add(i);
		vnew.add(allV.get(0));
		Edge chosenEdge;int chosenVertex;
		
		while(vnew.size()<allV.size()){
			List<PrimHelper> toSort=new ArrayList<PrimHelper>();
			double max=-1;chosenEdge=null;chosenVertex=0;
			for(int u:vnew){
				List<Double> temp=train.adjList.get(u);
				for(int j=0;j<temp.size();j++){
					if(!vnew.contains(new Integer(j))){
						double wt=temp.get(j);
						if(Double.compare(wt,max)>=0){
							max=wt;
							int parent=u;
							int other=j;
							chosenVertex=j;
							if(j==0||u==0) {
								if(j==0) {parent=j;other=u;}
								else {parent=u;other=j;}
							}
							chosenEdge=new Edge(parent,other,wt);
							PrimHelper ph= new PrimHelper(chosenEdge, chosenVertex,wt);
							toSort.add(ph);
						}
					}
				}
			}
			Collections.sort(toSort,new PrimComparator());
			vnew.add(toSort.get(0).chosenVertex);
			train.mst.add(toSort.get(0).chosenEdge);
		}
		fillParentHash();
	}
	/**
	 * Utility function to organise nodes as parent child
	 */
	void fillParentHash(){
		train.parent.put(0,train.totalAttNo);
		for(Edge e:train.mst){
			//put child -> parent
			train.parent.put(e.u,e.v);
		}
	}
	
	/**
	 * computer CPT of all attributes
	 */
	void computePdForTan(){
		int i=0;
		for(Map.Entry<String, LinkedHashMap<String,Pd>> entry:train.attributes.entrySet()){
			int parentIndex=train.parent.get(i);
			if(parentIndex==train.totalAttNo) {i++;continue;}
			String parentVal=train.featNames.get(parentIndex);
			for(Map.Entry<String,Pd> map:entry.getValue().entrySet()){
				String x_value=map.getKey();
				Pd obj=map.getValue();
				int x_index=obj.index;
				int len_x=entry.getValue().size();
				for(Map.Entry<String, Pd> mapinner:train.attributes.get(parentVal).entrySet()){
					String y_value=mapinner.getKey();
					Pd obj1=mapinner.getValue();
					int y_index=obj1.index;
					int cnt_x_parent_y=0;int cnt_x_parent_y_dash=0;
					int cnt_parent_y =0;int cnt_parent_y_dash=0;
					int len_y=train.attributes.get(parentVal).size();
					for(List<String> rowData:train.data){
						// calculate cnt_parent_y , cnt_parent_y_dash
						if(rowData.get(y_index).trim().equalsIgnoreCase(y_value.trim()) &&
								rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[0].trim())){
							cnt_parent_y++;
						}
						if(rowData.get(y_index).trim().equalsIgnoreCase(y_value.trim()) &&
								rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[1].trim())){
							cnt_parent_y_dash++;
						}
						
						//cnt_x_parent_y , cnt_x_parent_y_dash
						if(rowData.get(y_index).trim().equalsIgnoreCase(y_value.trim()) &&
								rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[0].trim()) &&
								rowData.get(x_index).trim().equalsIgnoreCase(x_value.trim())){
							cnt_x_parent_y++;
						}
						if(rowData.get(y_index).trim().equalsIgnoreCase(y_value.trim()) &&
								rowData.get(train.totalAttNo).trim().equalsIgnoreCase(train.classArr[1].trim()) &&
								rowData.get(x_index).trim().equalsIgnoreCase(x_value.trim())){
							cnt_x_parent_y_dash++;
						}
					}
					double pd1=laplace(cnt_x_parent_y,cnt_parent_y,len_x);
					double pd2=laplace(cnt_x_parent_y_dash,cnt_parent_y_dash,len_x);
					obj.hm.put(y_value,new TanMeta(pd1, pd2));
				}
				
			}
			i++;
		}
	}
	
	/**
	 * predict class based on tan
	 */
	void tanPredict(){
		int match=0;
		for(List<String> rowData:test.data){
			double prod1=1;double prod2=1;
			for(int i=0;i<rowData.size()-1;i++){
				String featName = train.featNames.get(i);
				String x_value=rowData.get(i);
				int parentIndex=train.parent.get(i);
				if(parentIndex==train.totalAttNo){
					Pd obj=train.attributes.get(featName).get(x_value);
					prod1*=obj.c1;
					prod2*=obj.c2;
				}
				else{
				String y_value=rowData.get(parentIndex);
				Pd obj=train.attributes.get(featName).get(x_value);
				prod1*=obj.hm.get(y_value).c1;
				prod2*=obj.hm.get(y_value).c2;
				}
			}
			NumberFormat formatter = new DecimalFormat("#0.000000000000");
			prod1*=train.classProb[0];
			prod2*=train.classProb[1];
			double posterior=prod1+prod2;
			double ans1=prod1/posterior;
			double ans2=prod2/posterior;
			
			if(prod1>prod2){
				if(train.classArr[0].trim().equalsIgnoreCase(rowData.get(train.totalAttNo))) match++;
				System.out.print(train.classArr[0]+" "+rowData.get(train.totalAttNo)+" ");
				System.out.println(formatter.format(ans1));
			}else{
				if(train.classArr[1].trim().equalsIgnoreCase(rowData.get(train.totalAttNo))) match++;
				System.out.print(train.classArr[1]+" "+rowData.get(train.totalAttNo)+" ");
				System.out.println(formatter.format(ans2));
			}
		}
		System.out.println();
		System.out.println(match);
	}
	
	/**
	 * print the network/tree constructed for TAN 
	 */
	void printTanNw(){
		System.out.println(train.featNames.get(0)+" "+"class");
		for(int i=1;i<train.totalAttNo;i++){
			String self= train.featNames.get(i);
			int parentIndex=train.parent.get(i);
			String parentVal= train.featNames.get(parentIndex);
			System.out.println(self+" "+parentVal+" class");
		}
	}
	/**
	 * method to get data needed to plot learning curves
	 */
	void plotGraphs(){
		int[] samples={25,50,100};
		for(int i=0;i<samples.length;i++){
			for(int k=0;k<4;k++){
				Collections.shuffle(train.data);
				List<List<String>> tempList= new ArrayList<List<String>>();
				for(int j=0;j<samples[i];j++){
					tempList.add(train.data.get(j));
				}
				//train on tempList and test on main test file . get accuracy and print it 
			}
		}
		
		
	}
	public static void main(String args[]){
		String trainFile=args[0];
		String testFile=args[1];
		String val=args[2];
		String naive="n";
		String tan="t";
		NaiveBeyes nb= new NaiveBeyes();
		if(val.trim().equalsIgnoreCase(naive)){
			//NB working with arff reader
			nb.parseInput(trainFile,nb.train);
			nb.computePd();
			nb.parseInput(testFile,nb.test);
			nb.predictClass();
		}
		else if(val.trim().equalsIgnoreCase(tan)){
			//tan working with arff reader
			nb.parseInput(trainFile,nb.train);
			nb.computePd();
			nb.computeWeight();
			nb.primMst();
			nb.computePdForTan();
//			nb.verify(nb.train);
			nb.parseInput(testFile,nb.test);
			nb.printTanNw();
			System.out.println();
			nb.tanPredict();
		}
		
	}
}
