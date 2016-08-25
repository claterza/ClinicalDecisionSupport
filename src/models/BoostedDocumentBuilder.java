package models;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;


public class BoostedDocumentBuilder {
  
  private Field typesField;
  private Field titleField;
  private Field keywordsField;
  private Field abstractField;
  private Field bodyField;
  private Field catField;
  private Field fileNameField;
  private Field pathField;
  
  /**
   * Parameter values default to empty string
   */
  public BoostedDocumentBuilder() {
    this.typesField = new TextField("types", "", Field.Store.YES);
    this.titleField = new TextField("title", "", Field.Store.YES);
    this.keywordsField = new TextField("keywords", "", Field.Store.YES);
    this.abstractField = new TextField("abstract", "", Field.Store.YES);
    this.bodyField = new TextField("body", "", Field.Store.YES);
    this.catField = new TextField("categories", "", Field.Store.YES);
    this.fileNameField = new StringField("filename", "", Field.Store.YES);
    this.pathField = new StringField("path", "", Field.Store.YES);
  }
  
  /**
   * Builds a BoostedDocument from the values put into the builder
   * @return A boosted document
   */
  public Document build() {
    Document doc = new Document();
    doc.add(this.abstractField);
    doc.add(this.bodyField);
    doc.add(this.catField);
    doc.add(this.fileNameField);
    doc.add(this.keywordsField);
    doc.add(this.pathField);
    doc.add(this.titleField);
    doc.add(this.typesField);
    return doc;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // builder methods
  /////////////////////////////////////////////////////////////////////////////

  
  public BoostedDocumentBuilder types(String type, float boostValue) {
    this.typesField = new TextField("types", type, Field.Store.YES);
    this.typesField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder title(String title, float boostValue) {
    this.titleField = new TextField("title", title, Field.Store.YES);
    this.titleField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder keywords(String keywords, float boostValue) {
    this.keywordsField = new TextField("keywords", keywords, Field.Store.YES);
    this.keywordsField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder abstractText(String abstractText, float boostValue) {
    this.abstractField = new TextField("abstract", abstractText, Field.Store.YES);
    this.abstractField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder body(String body, float boostValue) {
    this.bodyField = new TextField("body", body, Field.Store.YES);
    this.bodyField.setBoost(boostValue);
    return this;
  }
  
  public BoostedDocumentBuilder categories(String categories, float boostValue) {
    this.catField = new TextField("categories", categories, Field.Store.YES);
    this.catField.setBoost(boostValue);
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
