package uk.co.compendiumdev.javafortesters.javafx;

import uk.co.compendiumdev.javafortesters.counterstrings.CounterString;
import uk.co.compendiumdev.javafortesters.counterstrings.CounterStringCreationError;
import uk.co.compendiumdev.javafortesters.counterstrings.CounterStringRangeListIterator;
import uk.co.compendiumdev.javafortesters.counterstrings.CounterStringRangeStruct;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;


public class CounterStringStage extends Stage {

    private static CounterStringStage counterStringSingletonStage=null;

    public static void singletonActivate(){

        if(counterStringSingletonStage==null)
            counterStringSingletonStage = new CounterStringStage(false);

        counterStringSingletonStage.show();
        counterStringSingletonStage.requestFocus();
    }

    private class County{

        private List<CounterStringRangeStruct> range;
        private CounterStringRangeListIterator ranger;
        private CounterString counterstring;
        private String spacer;
        private Robot robot;

        public void createCounterStringRangesFor(int counterStringLength, String spacer) {
            this.counterstring = new CounterString();
            this.spacer = counterstring.getSingleCharSpacer(spacer);
            this.range = counterstring.createCounterStringRangesFor(counterStringLength, spacer);
            this.ranger = new CounterStringRangeListIterator(range);
            try {
                this.robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

        public boolean hasAnotherValueInRangeList() {
            return this.ranger.hasAnotherValueInRangeList();
        }

        public String getNextCounterStringEntry() {
            return counterstring.getCounterStringRepresentationOfNumber(ranger.getNextCounterStringValue(), this.spacer);
        }

        public Robot getRobot(){
            return this.robot;
        }
    }

    public CounterStringStage(boolean hidden) {

        BorderPane root = new BorderPane();

        HBox counterstringControl = new HBox();
        final Label lengthLabel = new Label("Length:");
        final TextField counterLength = new TextField ();
        counterLength.setTooltip(new Tooltip("The length of the CounterString to create"));
        counterLength.setText("100");
        Button createCounter = new Button();
        createCounter.setText("Create");
        createCounter.setTooltip(new Tooltip("Create a CounterString and display in the text area"));

        final TextField counterstringSpacer = new TextField ();
        counterstringSpacer.setText("*");
        counterstringSpacer.setTooltip(new Tooltip("The spacer value for the counterstring"));
        counterstringSpacer.setMaxWidth(50);
        addTextLimiter(counterstringSpacer,1);

        Button createClipboard = new Button();
        createClipboard.setText("=>");
        createClipboard.setTooltip(new Tooltip("Create direct to clipboard"));

        final Button copyCounterString = new Button();
        copyCounterString.setText("Copy");
        copyCounterString.setTooltip(new Tooltip("Copy the text in the text area to the clipboard"));

        counterstringControl.getChildren().addAll(lengthLabel, counterLength, counterstringSpacer,
                createCounter, createClipboard, copyCounterString);
        counterstringControl.setSpacing(10);


        HBox lengthControl = new HBox();
        final Button lenCounter = new Button();
        lenCounter.setText("Length?");
        lenCounter.setTooltip(new Tooltip("Count the number of characters in the text area below"));
        final Label lengthCount = new Label("");

        final Button clearTextArea = new Button();
        clearTextArea.setText("Clear");
        clearTextArea.setTooltip(new Tooltip("Clear the text from the text area"));


        final Button robotType = new Button();
        robotType.setText("Robot");
        robotType.setTooltip(new Tooltip("Have robot type counterstring into field"));

        lengthControl.getChildren().addAll(lenCounter, lengthCount, clearTextArea, robotType);
        lengthControl.setSpacing(10);



        final TextArea textArea = new TextArea("");
        textArea.setWrapText(true);

        VBox form = new VBox();
        form.getChildren().addAll(counterstringControl, lengthControl);

        root.setTop(form);
        root.setCenter(textArea);

        Scene scene = new Scene(root, Config.getDefaultWindowWidth(), Config.getDefaultWindowHeight());
        this.setTitle("Counterstrings");
        this.setScene(scene);
        if(!hidden)
            this.show();

        County county = new County();


        //robot typing into field- never stop thread - once robot is used a thread ticks over in the background
        // ready to be re-used
        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int x=5;
                while(true) {
                    if(robotType.getText().startsWith("Robot")){
                        x=5;
                    }
                    final int finalX = x--;

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {



                            // This needs to be a state machine
                            // Robot means stop don't do anything
                            // Start means start counting down
                            // In [ means continue counting
                            // GO means calculate the counterstring
                            // ... means iterate through and send the keys
                            if (robotType.getText().startsWith("Start") || robotType.getText().startsWith("In [")) {
                                robotType.setText("In [" + finalX + "] secs");
                            }
                            if (finalX <= 0) {
                                robotType.setText("GO");
                                // calculate counterstring and iterator here

                                robotType.setText("...");
                            }
                            if(robotType.getText().startsWith("...")){
                                if(county.hasAnotherValueInRangeList()){
                                    String outputString = county.getNextCounterStringEntry();
                                    robotType.setText("..."+outputString);
                                    //System.out.println(outputString);
                                    for(char c : outputString.toCharArray()) {
                                        //System.out.println(""+c);
                                        // this hack means that with robot we should really default to *
                                        if(c=='*'){
                                            county.getRobot().keyPress(KeyEvent.VK_SHIFT);
                                            county.getRobot().keyPress(KeyEvent.VK_8);
                                            county.getRobot().keyRelease(KeyEvent.VK_8);
                                            county.getRobot().keyRelease(KeyEvent.VK_SHIFT);
                                        }else {
                                            county.getRobot().keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
                                            county.getRobot().keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
                                        }
                                    }
                                }else{
                                    // we are finished
                                    robotType.setText("Robot");
                                }
                            }
                        }
                    });

                    if(robotType.getText().startsWith("Robot")||robotType.getText().startsWith("In [")) {
                        Thread.sleep(1000);
                    }else{
                        Thread.sleep(10);
                        x=5;
                    }
                    System.out.println("Thread " + x);
                }
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);





        robotType.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {

                        System.out.println("clicked robot");

                        try {

                            if(!th.isAlive()) {
                                th.start();
                            }

                            if(robotType.getText().startsWith("Robot")){

                                county.createCounterStringRangesFor(Integer.parseInt(counterLength.getText()), counterstringSpacer.getText());
                                robotType.setText("Start");

                            }else{
                                robotType.setText("Robot");
                            }

                        }
                        catch(NumberFormatException ex){
                            alertLengthNotNumeric();
                        }catch(Exception ex){
                            alertException(ex);
                        }
                    }
                });

        createClipboard.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        try {
                            int len = Integer.parseInt(counterLength.getText());
                            sendToClipboard(new CounterString().create(len,counterstringSpacer.getText()), copyCounterString);
                        }
                        catch(NumberFormatException ex){
                            alertLengthNotNumeric();
                        } catch (CounterStringCreationError counterStringCreationError) {
                            alertException(counterStringCreationError);
                        }catch(Exception ex){
                            alertException(ex);
                        }
                    }
                });

        copyCounterString.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        try {

                            sendToClipboard(textArea.getText(), copyCounterString);

                        } catch (NumberFormatException ex) {
                            alertLengthNotNumeric();
                        } catch (Exception ex) {
                            alertException(ex);
                        }

                    }
                });

        clearTextArea.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        try {

                            textArea.setText("");

                        } catch (Exception ex) {
                            alertException(ex);
                        }

                    }
                });

        lenCounter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    lengthCount.setText(String.valueOf(textArea.getText().length()));
                }catch (Exception ex){
                    alertException(ex);
                }
            }
        });

        createCounter.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {

                try {
                    int len = Integer.parseInt(counterLength.getText());
                    textArea.setText(new CounterString().create(len,counterstringSpacer.getText()));
                    copyCounterString.setText("Copy");
                }
                catch(NumberFormatException ex){
                    alertLengthNotNumeric();
                } catch (CounterStringCreationError counterStringCreationError) {
                    alertException(counterStringCreationError);
                }catch(Exception ex){
                    alertException(ex);
                }

            }
        });


    }




    //http://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength
    public void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
            }
        });
    }

    private void alertException(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void alertLengthNotNumeric() {
        // http://code.makery.ch/blog/javafx-dialogs-official/
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Length is not numeric");
        alert.setHeaderText(null);
        alert.setContentText("Length needs to be an integer");
        alert.showAndWait();
    }

    private void sendToClipboard(String contents, Button copyCounter) {
        copyCounter.setText("Copying");
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(contents);
        clipboard.setContent(content);
        copyCounter.setText("Copied");
    }


}
