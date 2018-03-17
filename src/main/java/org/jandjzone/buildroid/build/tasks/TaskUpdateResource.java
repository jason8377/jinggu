/*******************************************************************************
 * Copyright (C) 2018 Jason Luo
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package org.jandjzone.buildroid.build.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.google.gson.JsonSyntaxException;

/**
 * Implementation for altering XML node text or attribute value.
 * 
 * @author Jason
 *
 */
public class TaskUpdateResource extends BaseTask {
	private ResourceConfig resourceConfig;
	
	public TaskUpdateResource(ProjectInfo projectInfo, TaskInfo taskInfo) {
		super(projectInfo,taskInfo);
	}

	/**
	 * Check if must parameters are missing
	 * @return
	 */
	@Override
	protected List<String> checkArguments() throws JsonSyntaxException,Exception {
		List<String> missingArguments = new ArrayList<String>();
		
		resourceConfig = parseArguments(ResourceConfig.class);
		
		return missingArguments;
	}
	
	/**
	 * Run you command line task here
	 */
	@Override
	protected void run() throws Exception {
		if(resourceConfig == null
				|| StringUtils.isBlank(resourceConfig.getResource_path())
				|| resourceConfig.getUpdate_nodes() == null){
			throw new Exception("Resource update configuration is empty.");
		}
		String fileResourcePath = projectInfo.getProject_path() +"/" + resourceConfig.getResource_path();
		File fileResource = new File(fileResourcePath);
		if(!fileResource.exists() || !fileResource.isFile()){
			throw new Exception("File "+ fileResource + " doesn't exist.");
		}
		progressOutput("\"{}\"",fileResource.getAbsolutePath());
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(fileResourcePath);
		
		Node node = doc.getFirstChild();
		parseNode(node);
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fileResource);
		transformer.transform(source, result);
	}
	
	private boolean isAttributeMatch(NamedNodeMap attributeList, AttributeFilter[] filterList){
		//If filter list is empty, means match successfully.
		if(filterList == null || filterList.length == 0)return true;
		
		//Attribute list should not be empty.
		if(attributeList == null || attributeList.getLength() == 0)return false;
		
		for(AttributeFilter filter:filterList){
			if(!singleAttributeMatch(filter,attributeList))return false;
		}
		
		return true;
	}
	
	private String printAttributeList(AttributeFilter[] filterList){
		if(filterList == null || filterList.length == 0)return null;
		
		StringBuffer attributeList = new StringBuffer();
		for(AttributeFilter filter:filterList){
			if(attributeList.length()>0){
				attributeList.append(" ");
			}
			attributeList.append(filter.getAttr_name()).append("=\"")
			.append(filter.getAttr_value()).append("\"");
		}
		return attributeList.toString();
	}
	
	private boolean singleAttributeMatch(AttributeFilter filter, NamedNodeMap attributeList){
		//Any of those attributes(name or value) is empty will cause failure of matching
		if(filter.getAttr_name() == null || filter.getAttr_value() == null)return false;
		for(int i=0;i<attributeList.getLength();i++){
			Node attribute = attributeList.item(i);
			if(StringUtils.equals(attribute.getNodeName(), filter.getAttr_name())
					&& StringUtils.equals(attribute.getNodeValue(), filter.getAttr_value())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param node
	 */
	private void parseNode(Node node){
		String nodeNameHierachy = getNodeNameHierachy(node);
		//progressOutput("Node hierachy:{}",nodeNameHierachy);
		
		UpdateNode[] updateList = resourceConfig.getUpdate_nodes();
		for(UpdateNode updateNode:updateList){
			if(updateNode.getNode_location() == null)continue;
			if(updateNode.getNew_value() == null)continue;
			if(!updateNode.getNode_location().equals(nodeNameHierachy))continue;

			/**
			 * If nodeType is element means node text is going to be changed
			 */
			Node parentNode = node.getParentNode();
			if(parentNode != null){
			    if(parentNode.getNodeType() == Node.ATTRIBUTE_NODE){
			    	parentNode = hashMap.get(parentNode);
			    }
			}
		    AttributeFilter[] filterList = updateNode.getAttribute_filter();
			if(parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE){
			    //Check attribute filter
			    if(!isAttributeMatch(parentNode.getAttributes(),filterList)){
			    	progressOutput("Attribute list{} don't match.",printAttributeList(filterList));
			    	continue;
			    }
			}
		    String filterPrintList = printAttributeList(filterList);
		    if(filterPrintList != null){
			    progressOutput("Node \"{}\" matches the filters with attributes: [{}]" ,nodeNameHierachy ,filterPrintList);
		    }else{
		    	progressOutput("Node \"{}\" matches the filters." ,nodeNameHierachy);
		    }
		    node.setNodeValue(updateNode.getNew_value());
		    progressOutput("Value of node \"{}\" has been changed to \"{}\"" ,nodeNameHierachy,updateNode.getNew_value());
		}
		

		//Get all attributes
		NamedNodeMap attributeMap = node.getAttributes();
		if(attributeMap != null){
			for(int i=0;i<attributeMap.getLength();i++){
				Node attributeNode = attributeMap.item(i);
				
				hashMap.put(attributeNode, node);
				
				parseNode(attributeNode);
			}
		}
		
		/**
		 * Note that if the text of an element is empty, 
		 * calling getChildNodes will get 0 of length.
		 * 
		 * Like <string name="app_name"></string> or
		 * <string name="app_name" />
		 * 
		 * To solved it, just append a text node to it if its text is empty.
		 */
		if(node.getNodeType() == Node.ELEMENT_NODE && !node.hasChildNodes()){
			node.appendChild(node.getOwnerDocument().createTextNode(Constants.EMPTY_STRING));
		}
		
		NodeList nodeList = node.getChildNodes();
		if(nodeList != null && nodeList.getLength() > 0){
		    for(int i=0;i<nodeList.getLength();i++){
		    	Node childNode = nodeList.item(i);
		    	parseNode(childNode);
		    }
		}
	}
	
	private Map<Node,Node> hashMap = new HashMap<Node,Node>();
	
	private String getNodeNameHierachy(Node node){
		//String nodeName = node.getNodeName() + (node.getNodeType() ==3 ? "="+node.getNodeValue():"");
		String nodeName = node.getNodeName();
		Node parentNode = node.getParentNode();
		
		if(parentNode == null)parentNode = hashMap.get(node);
		
		if(parentNode != null && parentNode.getNodeType() != Node.DOCUMENT_NODE) {
			String nodeNameParent = getNodeNameHierachy(parentNode);
			nodeName = nodeNameParent + "->" + nodeName;
		}

		return nodeName;
	}
	
	private static class ResourceConfig{
		private String resource_path;
		private UpdateNode[] update_nodes;
		public String getResource_path() {
			return resource_path;
		}
		public void setResource_path(String resource_path) {
			this.resource_path = resource_path;
		}
		public UpdateNode[] getUpdate_nodes() {
			return update_nodes;
		}
		public void setUpdate_nodes(UpdateNode[] update_nodes) {
			this.update_nodes = update_nodes;
		}
	}
	private static class UpdateNode{
		private String node_location;
		private String new_value;
		private AttributeFilter[] attribute_filter;
		public String getNode_location() {
			return node_location;
		}
		public void setNode_location(String node_location) {
			this.node_location = node_location;
		}
		public String getNew_value() {
			return new_value;
		}
		public void setNew_value(String new_value) {
			this.new_value = new_value;
		}
		public AttributeFilter[] getAttribute_filter() {
			return attribute_filter;
		}
		public void setAttribute_filter(AttributeFilter[] attribute_filter) {
			this.attribute_filter = attribute_filter;
		}
	}
	private static class AttributeFilter{
		private String attr_name;
		private String attr_value;
		public String getAttr_name() {
			return attr_name;
		}
		public void setAttr_name(String attr_name) {
			this.attr_name = attr_name;
		}
		public String getAttr_value() {
			return attr_value;
		}
		public void setAttr_value(String attr_value) {
			this.attr_value = attr_value;
		}
	}
}
