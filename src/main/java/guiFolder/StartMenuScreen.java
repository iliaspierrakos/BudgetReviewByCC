package guiFolder;

import UserManagement.UserManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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

        final boolean wasShowing = stage.isShowing();
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        Rectangle vignette = new Rectangle();
        vignette.getStyleClass().add("start-vignette");
        vignette.setManaged(false);

        Rectangle grid = new Rectangle();
        grid.getStyleClass().add("start-grid");
        grid.setManaged(false);

        Rectangle sweep = new Rectangle();
        sweep.getStyleClass().add("start-sweep");
        sweep.setManaged(false);

        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("/guiFolder/logo1.png");
        if (logoStream != null) logo.setImage(new Image(logoStream));

        logo.setFitWidth(320);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("start-logo");

        StackPane logoFrame = new StackPane(logo);
        logoFrame.getStyleClass().add("start-logo-frame");

        Label title = new Label("Prime Minister for a Day");
        title.getStyleClass().addAll("title", "start-title");

        Label subtitle = new Label("Budget Review Simulator");
        subtitle.getStyleClass().addAll("subtitle", "start-subtitle");

        Separator sep = new Separator();
        sep.getStyleClass().add("start-sep");

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
        exitButton.setOnAction(e -> Platform.exit());

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

        StackPane root = new StackPane(vignette, grid, sweep, card);
        root.getStyleClass().add("start-root");

        root.widthProperty().addListener((obs, o, w) -> {
            double ww = w.doubleValue();
            vignette.setWidth(ww);
            grid.setWidth(ww);
            sweep.setWidth(ww);
        });
        root.heightProperty().addListener((obs, o, h) -> {
            double hh = h.doubleValue();
            vignette.setHeight(hh);
            grid.setHeight(hh);
            sweep.setHeight(hh);
        });

        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(24));

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 720, 560);
            safeAddCss(scene, "/css/DarkTheme.css");
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            safeAddCss(scene, "/css/DarkTheme.css");
        }

        stage.setTitle("Welcome");
        stage.show();

        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else if (wasShowing) {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        } else {
            stage.centerOnScreen();
        }

        ScaleTransition st = new ScaleTransition(Duration.seconds(3), logoFrame);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.03);
        st.setToY(1.03);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();

        card.setOpacity(0);
        card.setTranslateY(18);

        FadeTransition ft = new FadeTransition(Duration.millis(420), card);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(420), card);
        tt.setFromY(18);
        tt.setToY(0);

        new ParallelTransition(ft, tt).play();

        sweep.setTranslateX(-900);
        Timeline sweepAnim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sweep.translateXProperty(), -900)),
                new KeyFrame(Duration.seconds(2.6), new KeyValue(sweep.translateXProperty(), 900))
        );
        sweepAnim.setCycleCount(Animation.INDEFINITE);
        sweepAnim.setDelay(Duration.seconds(0.4));
        sweepAnim.play();
    }

    private static void safeAddCss(Scene scene, String cssPath) {
        var url = StartMenuScreen.class.getResource(cssPath);
        if (url == null) return;
        String css = url.toExternalForm();
        if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
    }
}
