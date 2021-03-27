package com.kaaphi.cocktails.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

public class Recipe implements Serializable {
  private static final long serialVersionUID = 1L;

  private ObjectId id;
  private String name;
  private String instructions;
  private String reference;
  private String referenceDetail;
  private String note;
  private boolean indexElements;
  private List<RecipeElement> elements;
  private List<String> tags;
  private boolean archived;

  public Recipe() {

  }

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
      if(e.getIsBase()) {
        base.add(e);
      }
    }

    return base;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
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

  @BsonIgnore
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

  @BsonIgnore
  public void setTagString(String tagString) {
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Recipe recipe = (Recipe) o;
    return indexElements == recipe.indexElements && archived == recipe.archived && Objects
        .equals(id, recipe.id) && Objects.equals(name, recipe.name) && Objects
        .equals(instructions, recipe.instructions) && Objects
        .equals(reference, recipe.reference) && Objects
        .equals(referenceDetail, recipe.referenceDetail) && Objects
        .equals(note, recipe.note) && Objects.equals(elements, recipe.elements)
        && Objects.equals(tags, recipe.tags);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id, name, instructions, reference, referenceDetail, note, indexElements, elements,
            tags, archived);
  }
}
