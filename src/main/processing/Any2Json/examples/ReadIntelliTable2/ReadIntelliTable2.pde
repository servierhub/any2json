import com.github.romualdrousseau.shuju.json.*;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.classifiers.*;

import com.github.romualdrousseau.any2json.v2.*;
import com.github.romualdrousseau.any2json.v2.base.*;
import com.github.romualdrousseau.any2json.v2.intelli.*;
import com.github.romualdrousseau.any2json.v2.intelli.event.*;
import com.github.romualdrousseau.any2json.v2.intelli.header.*;
import com.github.romualdrousseau.any2json.v2.layex.*;

import java.util.List;
import java.awt.event.KeyEvent;

ITagClassifier classifier;
int scrollSpeed;
int gridSize;

volatile boolean documentLoaded = true;
PGraphics documentImage;
int documentTopY;

void configure() {
  classifier = new NGramNNClassifier(JSON.loadJSONObject(dataPath("brainColumnClassifier.json")));

  scrollSpeed = 100; // 100px per scroll  

  gridSize = 10; // 10px
}

void setup() {
  size(1000, 800);
  noSmooth();
  frameRate(20);

  configure();

  buildEmptyImage();
}

void draw() {
  if (!documentLoaded) {
    return;
  }

  background(51);
  image(documentImage, 0, -documentTopY);
  displayHUD();
}

void fileSelected(File selection) {
  if (selection != null) {
    noLoop();
    documentLoaded = false;
    loadDocument(selection.getAbsolutePath());
    documentTopY = 0;
    documentLoaded = true;
    loop();
  }
}

void keyPressed() {
  if (keyCode == KeyEvent.VK_F3) {
    selectInput("Select a file to process:", "fileSelected");
  }
  if (keyCode == KeyEvent.VK_HOME) {
    documentTopY = 0;
  }
  if (keyCode == KeyEvent.VK_END) {
    documentTopY = max(0, documentImage.height - height + 17);
  }
  if (keyCode == KeyEvent.VK_PAGE_UP ) {
    documentTopY = (int) constrain(documentTopY - height, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
  }
  if (keyCode == KeyEvent.VK_PAGE_DOWN ) {
    documentTopY = (int) constrain(documentTopY + height, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
  }
}

void mouseWheel(MouseEvent event) {
  documentTopY = (int) constrain(documentTopY + event.getCount() * scrollSpeed, -gridSize, max(0, documentImage.height - height + 17) + gridSize);
}

void loadDocument(String filePath) {
  println("Loading document ... ");
  Document document = DocumentFactory.createInstance(filePath, "UTF-8");

  Sheet sheet = document.getSheetAt(0);
  sheet.addSheetListener(new SheetListener() {
    public void stepCompleted(SheetEvent e) {
      buildImage(e);
    }
  }
  );

  com.github.romualdrousseau.any2json.v2.Table table = sheet.getTable(classifier);
  println("Tables loaded.");
  println("done.");

  document.close();

  println();
  for (Header header : table.headers()) {
    print(header.getName(), " ");
  }
  println();
}

void buildEmptyImage() {
  int dx = width / classifier.getSampleCount();
  int dy = gridSize;
  
  documentImage = createGraphics(width, height);

  documentImage.beginDraw();
  documentImage.stroke(128);
  documentImage.strokeWeight(1);
  for (int y = 0; y < height / dy; y++) {
    for (int x = 0; x < width / dx; x++) {
      documentImage.fill(0);
      documentImage.rect(x * dx, y * dy, dx, dy);
    }
  }
  documentImage.endDraw();
}

void buildImage(SheetEvent e) {
  IntelliSheet sheet = (IntelliSheet) e.getSource();
  int dx = width / classifier.getSampleCount();

  if (e instanceof BitmapGeneratedEvent) {
    SheetBitmap bitmap = ((BitmapGeneratedEvent) e).getBitmap();

    // Max rows set to 5000 to prevent heap overflow
    documentImage = createGraphics(width, Math.min(sheet.getLastRowNum(), 5000) * gridSize);

    documentImage.beginDraw();
    documentImage.stroke(128);
    documentImage.strokeWeight(1);
    for (int y = 0; y < bitmap.getHeight(); y++) {
      for (int x = 0; x < bitmap.getWidth(); x++) {
        documentImage.fill(color(255 * bitmap.get(x, y)));
        documentImage.rect(x * dx, y * gridSize, dx, gridSize);
      }
    }
    documentImage.endDraw();
    
    println("Image generated.");
  }

  if (e instanceof AllTablesExtractedEvent) {
    documentImage.beginDraw();
    documentImage.stroke(0, 0, 255);
    documentImage.strokeWeight(2);
    documentImage.noFill();
    for (AbstractTable table : ((AllTablesExtractedEvent) e).getTables()) {
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
    }
    documentImage.endDraw();

    println("Tables extracted from image.");
  }
  
  if(e instanceof DataTableListBuiltEvent) {
    documentImage.beginDraw();
    documentImage.noStroke();
    
    for (DataTable table : ((DataTableListBuiltEvent) e).getDataTables()) {
      documentImage.fill(color(255, 128, 0), 128);
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getFirstRowOffset() * gridSize);
      documentImage.fill(color(0, 255, 0), 128);
      documentImage.rect(table.getFirstColumn() * dx, (table.getFirstRow() + table.getHeaderRowOffset()) * gridSize, table.getNumberOfColumns() * dx, gridSize);
      documentImage.fill(color(0, 255, 0), 64);
      documentImage.rect(table.getFirstColumn() * dx, (table.getFirstRow() + table.getFirstRowOffset()) * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
      documentImage.fill(color(0, 0, 0), 64);
      documentImage.rect(table.getFirstColumn() * dx, (table.getLastRow() + table.getLastRowOffset() + 1) * gridSize, table.getNumberOfColumns() * dx, -table.getLastRowOffset() * gridSize);
    }
    documentImage.endDraw();

    println("DataTable list built.");
  
  }
  
  if(e instanceof MetaTableListBuiltEvent) {
    documentImage.beginDraw();
    documentImage.noStroke();
    documentImage.fill(color(255, 128, 0), 128);
    for (MetaTable table : ((MetaTableListBuiltEvent) e).getMetaTables()) {
      documentImage.rect(table.getFirstColumn() * dx, table.getFirstRow() * gridSize, table.getNumberOfColumns() * dx, table.getNumberOfRows() * gridSize);
    }
    documentImage.endDraw();
    
    println("MetaTable list built.");
  
  }

  if (e instanceof TableGraphBuiltEvent) {
    println("TableGraph generated.");
    println("============================ DUMP TABLEGRAPH ============================");
    ((TableGraphBuiltEvent) e).dumpTableGraph();
    println("================================== END ==================================");
  }
}

void displayHUD() {
  fill(0);
  stroke(255);
  rect(0, height - 17, width - 1, 16);

  fill(255);
  text("F3: Open a document", 4, height - 4);

  fill(255, 0, 0);
  int x = floor(mouseX * classifier.getSampleCount() / width);
  int y = floor((mouseY + documentTopY) / gridSize);
  String s = String.format("(%d, %d)", x, y);
  text(s, width - textWidth(s) - 4, height - 4);
}