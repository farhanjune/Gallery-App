// import statements
import javafx.scene.Scene;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.event.EventHandler;
import java.io.FileNotFoundException;
import javafx.animation.KeyFrame;
import javafx.geometry.Orientation;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import javafx.util.Duration;
import javafx.scene.control.ToolBar;
import javafx.animation.Animation;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.google.gson.*;

/**
 * This class creates a GUI that represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {
    VBox vBox;
    HBox hBox;
    Menu file;
    String input;
    Timeline timeline;
    Button newButton;
    Button pausePlayButton;
    int progressIndicator;
    boolean play = true;
    String[] results;
    String[] artworkURLs = new String[20];
    TilePane newPane = new TilePane();
    ProgressBar progressBar = new ProgressBar();
    ImageView[] imageViewArray = new ImageView[20];
    double playCounter = -1.0;
    double progress = 0.0;
    JsonArray newArr;
    JsonArray oldArr;
    Scene scene;
    Stage stage1;
    MenuBar menuBar;
    TextField textField;
    JsonArray jsonResults;
    ToolBar toolBar;

    /**
     * Starts the program with stage.
     * 
     * @param stage input stage
     */
    @Override
    public void start(Stage stage) {
        vBox = new VBox();
        HBox holder = new HBox();
        GridPane grid1 = new GridPane();
        TilePane tile1 = new TilePane();
        ToolBar toolBarSearch = new ToolBar();
        ProgressBar p1 = new ProgressBar(0);
        TextField searchBar = new TextField();
        vBox.getChildren().addAll(newMenuBar(), newToolBar());
        vBox.getChildren().add(newProgressBar());
        Separator line = new Separator();
        line.setOrientation(Orientation.VERTICAL);
        Thread t = new Thread(() -> {
            getImages(input);
            Platform.runLater(() -> {
                vBox.getChildren().add(paneUpdate());
            });
        });
        t.setDaemon(true);
        t.start();
        scene = new Scene(vBox, 500, 480); // dimensions of scene
        stage.setTitle("Gallery!");
        stage.setMaxWidth(1280); // max dimensions
        stage.setMaxHeight(720);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * Getter for stage object.
     * 
     * @return GUI stage
     */
    public Stage getStage() {
        return stage1;
    } // getStage

    /**
     * Replaces images on timeline randomly.
     */
    public void runPlay() {
        EventHandler<ActionEvent> handler = (e -> {
            if (jsonResults.size() > 21) { // pause or play based on number
                if (pausePlayButton.getText() == "Play") {
                    timeline.pause();
                    return;
                }
                int num = (int) (Math.random() * imageViewArray.length);
                // random number replacing
                int randomnewArr = (int) (Math.random() * newArr.size());
                JsonObject result = newArr.get(randomnewArr).getAsJsonObject();
                JsonElement artworkUrl100 = result.get("artworkUrl100");
                if (artworkUrl100 != null) {
                    randomImage(artworkUrl100, num);
                }
                if (oldArr.contains(newArr.get(randomnewArr))) {
                    newArr.remove(newArr.get(randomnewArr));
                }
            }
        });
        setTimeline(handler);
    }

    /**
     * Adds components to menubar.
     * 
     * @return a menubar
     */
    public MenuBar newMenuBar() {
        Menu file = new Menu("File");
        menuBar = new MenuBar();
        // creates menubar and adds items
        menuBar.getMenus().add(file);
        MenuItem menuItem = new MenuItem("Exit");
        file.getItems().add(menuItem);
        menuItem.setOnAction(event -> System.exit(0));
        return menuBar;
    }

    /**
     * Play/pause button event handler.
     */
    public void playPauseChange() {
        playCounter++;
        if (playCounter % 2 == 0.0) { 
            // if playCounter is even, runPlay()
            play = true;
        } else {
            play = false;
        }
        Platform.runLater(() -> {
            if (play) {
                pausePlayButton.setText("Pause");
                timeline.play();
            } else {
                pausePlayButton.setText("Play");
                timeline.pause();
                return;
            }
        });
        if (play) {
            runPlay();
        }
    }

    /**
     * Makes a progressbar and stages it.
     * 
     * @return progressbar with set layout
     */
    public HBox newProgressBar() {
        hBox = new HBox();
        // set progressbar dimensions
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(550.0);
        hBox.getChildren().add(progressBar);
        return hBox;
    }

    /**
     * Makes the timeline comtinue playing.
     */
    public void resume() {
        timeline.play();
    } // resume

    /**
     * Pauses the timeline.
     */
    public void pause() {
        timeline.pause();
    } // pause

    /**
     * Increments progress for progress bar.
     */
    public void incrementProgress() {
        progressIndicator++;
        // increments progressbar by 0.05
        progress = progress + 0.05;
        progressBar.setProgress(progress);
    }

    /**
     * Adds toolbar with components.
     * 
     * @return toolbar with components
     */
    public ToolBar newToolBar() {
        toolBarComponents();
        // adds toolbar with components for pause/play
        pausePlayButton.setOnAction(e -> {
            playPauseChange();
        });
        newButton.setOnAction(e -> {
            boolean runStatus = false;
            if (timeline != null) {
                if (timeline.getStatus() == Animation.Status.RUNNING) {
                    runStatus = true;
                    timeline.pause();
                }
            }
            // adds to container
            String newInput = readinput(textField);
            progressBar.setProgress(0.0);
            Thread t = new Thread(() -> {
                getImages(newInput);
                Platform.runLater(() -> {
                    paneUpdate();
                });
            });
            t.setDaemon(true);
            t.start();
            // sets actions
            if (runStatus) {
                timeline.play();
            }
        });
        return toolBar;
    }

    /**
     * Makes toolbar components.
     */
    public void toolBarComponents() {
        // adds the toolbar components 
        toolBar = new ToolBar();
        newButton = new Button("Update Images");
        pausePlayButton = new Button("Play");
        Label searchQuery = new Label("Search Query:");
        textField = new TextField("rock");
        input = readinput(textField);
        toolBar.getItems().addAll(pausePlayButton, searchQuery, textField, newButton);
    }

    /**
     * Reads user input.
     *
     * @param textField textfield for user input.
     * @return user input
     */
    public String readinput(TextField textField) {
        input = textField.getText();
        String[] array = input.split(" ");
        input = "";
        // reads through string array
        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                input = input + array[i];
                continue;
            }
            input = input + "+" + array[i];
        }
        progress = 0.0;
        return input;
    }

    /**
     * Sets timeline.
     *
     * @param handler lambda expression that the timeline accepts
     */
    public void setTimeline(EventHandler<ActionEvent> handler) {
        // sets timeline to update every 2 seconds
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * This method initializes Imageviews.
     */
    public void intialImageArray() {
        for (int i = 0; i < 20; i++) {
            imageViewArray[i] = new ImageView(new Image(artworkURLs[i]));
        }
    }

    /**
     * Updates images based on user input.
     * 
     * @return pane with updaed images
     */
    public TilePane paneUpdate() {
        int paneResults = results.length;
        if (paneResults < 21) {
            return newPane;
        }
        progress = 0.0;
        newPane.getChildren().clear();
        // if image count is under 20
        for (int x = 0; x < 20; x++) {
            imageViewArray[x].setImage(new Image(results[x]));
            // set new image
            newPane.getChildren().add(imageViewArray[x]);
        }
        for (int y = 0; y < newArr.size(); y++) {
            if (oldArr.contains(newArr.get(y))) {
                newArr.remove(newArr.get(y));
            }
        }
        return newPane;
    }

    /**
     * Helper method to read search query results.
     *
     * @param url    image link
     * @param qStr   string with parsed url link
     * @param reader reader for query results
     */
    public void parseUpdater(URL url, String qStr, InputStreamReader reader) {
        try {
            url = new URL(qStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
        try {
            reader = new InputStreamReader(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        // root of response
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(reader);
        JsonObject root = je.getAsJsonObject();
        jsonResults = root.getAsJsonArray("results");
        newArr = root.getAsJsonArray("results");
        oldArr = new JsonArray();
        results = new String[jsonResults.size()];
    }

    /**
     * Fills array with string URLs.
     *
     * @param resultNum number of image url
     */
    public void urlResults(int resultNum) {
        JsonObject result = jsonResults.get(resultNum).getAsJsonObject();
        oldArr.add(result);
        JsonElement artworkUrl100 = result.get("artworkUrl100");
        // adds JSON query results to array
        if (artworkUrl100 != null) {
            String artUrl = artworkUrl100.getAsString();
            Image newUrlImage = new Image(artUrl);
            results[resultNum] = artUrl;
            imageViewArray[resultNum] = new ImageView();
            imageViewArray[resultNum].setImage(new Image(results[resultNum]));
            Platform.runLater(() -> incrementProgress());
        }
    }

    /**
     * Reads search query results.
     *
     * @param input search entry of user
     */
    public void getImages(String input) {
        newPane.setPrefRows(4);
        newPane.setPrefColumns(5);
        InputStreamReader reader = null;
        URL url = null;
        String qStr = "https://itunes.apple.com/search?term=" + input;
        // adds user input to itunes url
        parseUpdater(url, qStr, reader);
        if (results.length < 21) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, 
                    "Error: Not enough search results found.",
                        ButtonType.OK);
                        // error for when results are under 21
                alert.showAndWait();
                pausePlayButton.setText("Play");
            });
            return;
        }
        for (int i = 0; i < 20; i++) {
            urlResults(i);
        }
    }

    /**
     * Takes in Json Element random image and replaces with one that has not been
     * used.
     *
     * @param artworkUrl100 Json Element
     * @param num           integer value of new image
     */
    public void randomImage(JsonElement artworkUrl100, int num) {
        oldArr.add(artworkUrl100);
        newArr.add(jsonResults.get(num));
        String artUrl = artworkUrl100.getAsString();
        Image imageUrl = new Image(artUrl);
        // new image with set values
        imageViewArray[num] = new ImageView();
        imageViewArray[num].setImage(imageUrl);
        imageViewArray[num].setFitHeight(100);
        imageViewArray[num].setFitWidth(100);
        newPane.getChildren().clear();
        for (int i = 0; i < 20; i++) {
            newPane.getChildren().add(imageViewArray[i]);
        }
    }

    /**
     * Default pane for when app starts.
     * 
     * @return a tilepane with default search
     */
    public TilePane getStarter() {
        return newPane;
    } // getStarter

} // GalleryApp
