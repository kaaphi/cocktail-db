package com.kaaphi.cocktails.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Recipe implements Serializable {
  private static final long serialVersionUID = 1L;

  private String name;
  private String instructions;
  private String reference;
  private String referenceDetail;
  private String note;
  private boolean indexElements;
  private List<RecipeElement> elements;
  private List<String> tags;
  private boolean archived;



  public Recipe(String name, String instructions, String reference,String referenceDetail, String note, List<String> tags, boolean indexElements, boolean archived) {
    this.name = name;
    this.instructions = instructions;
    this.reference = reference;
    this.referenceDetail = referenceDetail;
    this.note = note;
    this.tags = tags;
    this.elements = new LinkedList<RecipeElement>();
    this.indexElements = indexElements;
    this.archived = archived;
  }

  public Recipe(String name) {
    this(name, "", "", "", "", new ArrayList<String>(), true, false);
  }

  public List<RecipeElement> getBaseSpirits() {
    List<RecipeElement> base = new LinkedList<RecipeElement>();

    for(RecipeElement e : elements) {
      if(e.isBase()) {
        base.add(e);
      }
    }

    return base;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public boolean getIndexElements() {
    return indexElements;
  }

  public void setIndexElements(boolean indexElements) {
    this.indexElements = indexElements;
  }

  public List<String> getTags() {
    return tags;
  }

  public String getTagString() {
    Iterator<String> it = tags.iterator();

    if(!it.hasNext()) {
      return "";
    }

    StringBuilder sb = new StringBuilder(it.next());

    while(it.hasNext()) {
      sb.append(", ");
      sb.append(it.next());
    }

    return sb.toString();
  }

  public void setTagsFromString(String tagString) {
    if(tagString.trim().isEmpty()) {
      tags = new ArrayList<String>();
    } else { 
      tags = new ArrayList<String>(Arrays.asList(tagString.split("\\s*,\\s*")));
    }
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getReferenceWithDetail() {
    if(referenceDetail.isEmpty()) {
      return reference;
    } else { 
      return String.format("%s (%s)", reference, referenceDetail);
    }
  }

  public String getReferenceDetail() {
    return referenceDetail;
  }

  public void setReferenceDetail(String referenceDetail) {
    this.referenceDetail = referenceDetail;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public void addRecipeElement(RecipeElement element) {
    elements.add(element);
  }

  public void setRecipeElements(List<RecipeElement> elements) {
    this.elements = elements;
  }

  public List<RecipeElement> getRecipeElements() {
    return elements;
  }
  
  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(elements, indexElements, instructions, name, note, reference,
        referenceDetail, tags);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Recipe)) {
      return false;
    }
    Recipe other = (Recipe) obj;
    return Objects.equals(elements, other.elements) && indexElements == other.indexElements
        && Objects.equals(instructions, other.instructions) && Objects.equals(name, other.name)
        && Objects.equals(note, other.note) && Objects.equals(reference, other.reference)
        && Objects.equals(referenceDetail, other.referenceDetail)
        && Objects.equals(tags, other.tags);
  }



}
