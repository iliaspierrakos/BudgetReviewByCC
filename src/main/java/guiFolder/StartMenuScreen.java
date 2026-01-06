package guiFolder;

import UserManagement.UserManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartMenuScreen {
    private final UserManager userManager;

    public StartMenuScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // ---------- Background layers (WOW) ----------
        Rectangle vignette = new Rectangle();
        vignette.getStyleClass().add("start-vignette");
        vignette.setManaged(false);

        Rectangle grid = new Rectangle();
        grid.getStyleClass().add("start-grid");
        grid.setManaged(false);

        Rectangle sweep = new Rectangle();
        sweep.getStyleClass().add("start-sweep");
        sweep.setManaged(false);

        // ---------- Logo ----------
        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("/guiFolder/logo1.png");
        if (logoStream != null) logo.setImage(new Image(logoStream));

        logo.setFitWidth(320);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("start-logo");

        StackPane logoFrame = new StackPane(logo);
        logoFrame.getStyleClass().add("start-logo-frame");

        // ---------- Titles ----------
        Label title = new Label("Prime Minister for a Day");
        title.getStyleClass().addAll("title", "start-title");

        Label subtitle = new Label("Budget Review Simulator");
        subtitle.getStyleClass().addAll("subtitle", "start-subtitle");

        Separator sep = new Separator();
        sep.getStyleClass().add("start-sep");

        // ---------- Buttons (MATCH YOUR CSS) ----------
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("button", "primary", "start-btn");

        Button registerButton = new Button("Create account");
        registerButton.getStyleClass().addAll("button", "subtle", "start-btn");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().addAll("button", "danger", "start-btn");

        loginButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> new LoginScreen(userManager).show(stage));
        registerButton.setOnAction(e -> new RegisterScreen(userManager).show(stage));
        exitButton.setOnAction(e -> {
            stage.close();
            System.exit(0);
        });

        // ---------- Card ----------
        VBox cardContent = new VBox(
                14,
                logoFrame,
                title,
                subtitle,
                sep,
                loginButton,
                registerButton,
                exitButton
        );
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setFillWidth(true);
        cardContent.setPadding(new Insets(26, 36, 30, 36));

        VBox card = new VBox(cardContent);
        card.getStyleClass().addAll("card", "start-card");
        card.setMaxWidth(520);

        // Rounded clip
        Rectangle clip = new Rectangle();
        clip.setArcWidth(26);
        clip.setArcHeight(26);
        card.setClip(clip);
        card.layoutBoundsProperty().addListener((obs, o, b) -> {
            clip.setWidth(b.getWidth());
            clip.setHeight(b.getHeight());
        });

        DropShadow shadow = new DropShadow(55, Color.rgb(0, 0, 0, 0.65));
        shadow.setOffsetY(18);
        card.setEffect(shadow);

        // ---------- Root ----------
        StackPane root = new StackPane(vignette, grid, sweep, card);
        root.getStyleClass().add("start-root");

        // Bind background rectangles to scene size
        root.widthProperty().addListener((obs, o, w) -> {
            vignette.setWidth(w.doubleValue());
            grid.setWidth(w.doubleValue());
            sweep.setWidth(w.doubleValue());
        });
        root.heightProperty().addListener((obs, o, h) -> {
            vignette.setHeight(h.doubleValue());
            grid.setHeight(h.doubleValue());
            sweep.setHeight(h.doubleValue());
        });

        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(24));

        Scene scene = new Scene(root, 720, 560);
        scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        stage.setTitle("Welcome");
        stage.setScene(scene);
        stage.show();
        ScaleTransition st = new ScaleTransition(Duration.seconds(3), logoFrame);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.03);
        st.setToY(1.03);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();
        stage.centerOnScreen();

        // ---------- Entrance animation ----------
        card.setOpacity(0);
        card.setTranslateY(18);

        FadeTransition ft = new FadeTransition(Duration.millis(420), card);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(420), card);
        tt.setFromY(18);
        tt.setToY(0);

        new ParallelTransition(ft, tt).play();

        // ---------- Light sweep animation (WOW) ----------
        sweep.setTranslateX(-900);
        Timeline sweepAnim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sweep.translateXProperty(), -900)),
                new KeyFrame(Duration.seconds(2.6), new KeyValue(sweep.translateXProperty(), 900))
        );
        sweepAnim.setCycleCount(Animation.INDEFINITE);
        sweepAnim.setDelay(Duration.seconds(0.4));
        sweepAnim.play();
    }
}
