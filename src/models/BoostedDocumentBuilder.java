package models;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;


public class BoostedDocumentBuilder {
  
  private Field jTitleField;
  private Field aTitleField;
  private Field abstractField;
  private Field chemListField;
  private Field mhListField;
  private Field typeField;
  private Field fileNameField;
  private Field pathField;
  
  /**
   * Parameter values default to empty string
   */
  public BoostedDocumentBuilder() {
    this.typeField = new TextField("type", "", Field.Store.YES);
    this.jTitleField = new TextField("journalTitle", "", Field.Store.YES);
    this.aTitleField = new TextField("articleTitle", "", Field.Store.YES);
    this.abstractField = new TextField("abstract", "", Field.Store.YES);
    this.chemListField = new TextField("chemList", "", Field.Store.YES);
    this.mhListField = new TextField("mhList", "", Field.Store.YES);
    this.fileNameField = new StringField("filename", "", Field.Store.YES);
    this.pathField = new StringField("path", "", Field.Store.YES);
  }
  
  /**
   * Builds a BoostedDocument from the values put into the builder
   * @return A boosted document
   */
  public Document build() {
    Document doc = new Document();
    doc.add(this.typeField);
    doc.add(this.jTitleField);
    doc.add(this.aTitleField);
    doc.add(this.abstractField);
    doc.add(this.chemListField);
    doc.add(this.mhListField);
    doc.add(this.fileNameField);
    doc.add(this.pathField);
    return doc;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // builder methods
  /////////////////////////////////////////////////////////////////////////////

  
  public BoostedDocumentBuilder type(String type, float boostValue) {
    this.typeField = new TextField("type", type, Field.Store.YES);
    this.typeField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder journalTitle(String title, float boostValue) {
    this.jTitleField = new TextField("journalTitle", title, Field.Store.YES);
    this.jTitleField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder articleTitle(String title, float boostValue) {
    this.aTitleField = new TextField("articleTitle", title, Field.Store.YES);
    this.aTitleField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder abstractText(String abstractText, float boostValue) {
    this.abstractField = new TextField("abstract", abstractText, Field.Store.YES);
    this.abstractField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder chemicals(String chemicals, float boostValue) {
    this.chemListField = new TextField("chemList", chemicals, Field.Store.YES);
    this.chemListField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder headings(String mheadings, float boostValue) {
    this.mhListField = new TextField("mhList", mheadings, Field.Store.YES);
    this.mhListField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder fileName(String fileName) {
    this.fileNameField = new StringField("fileName", fileName, Field.Store.YES);
    return this;
  }
  
  public BoostedDocumentBuilder path(String path) {
    this.pathField = new StringField("path", path, Field.Store.YES);
    return this;
  }

  
}
