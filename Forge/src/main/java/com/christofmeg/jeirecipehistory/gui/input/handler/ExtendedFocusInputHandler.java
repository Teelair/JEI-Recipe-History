package com.christofmeg.jeirecipehistory.gui.input.handler;

import com.christofmeg.jeirecipehistory.gui.jei.JeiRecipeHistoryPlugin;
import com.christofmeg.jeirecipehistory.recipe.IRecipeInfo;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.FocusInputHandler;
import mezz.jei.input.mouse.handlers.LimitedAreaInputHandler;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Based on {@link FocusInputHandler}, but with some modifications to facilitate reading recipes from {@link IRecipeInfo}.
 */
public class ExtendedFocusInputHandler implements IUserInputHandler {

    private static final List<RecipeIngredientRole> SHOW_RECIPE_ROLES = List.of(RecipeIngredientRole.OUTPUT);
    private static final List<RecipeIngredientRole> SHOW_USES_ROLES = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST);

    private final CombinedRecipeFocusSource focusSource;
    private final RecipesGui recipesGui;

    @SuppressWarnings("unused")
    public static IUserInputHandler create(CombinedRecipeFocusSource focusSource, RecipesGui recipesGui) {
        return new ExtendedFocusInputHandler(focusSource, recipesGui);
    }

    public ExtendedFocusInputHandler(CombinedRecipeFocusSource focusSource, RecipesGui recipesGui) {
        this.focusSource = focusSource;
        this.recipesGui = recipesGui;
    }

    @SuppressWarnings("unused")
    public @NotNull Optional<IUserInputHandler> handleUserInput(@NotNull Screen screen, @NotNull UserInput input) {
        return handleOriginalShow(input);
    }

    private Optional<IUserInputHandler> handleOriginalShow(UserInput input) {
        return input.is(KeyBindings.showRecipe) ? handleShow(input, SHOW_RECIPE_ROLES)
                : input.is(KeyBindings.showUses) ? handleShow(input, SHOW_USES_ROLES)
                : Optional.empty();
    }

    private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles) {
        boolean simulate = input.isSimulate();
        Optional<IClickedIngredient<?>> optionalClicked = focusSource.getIngredientUnderMouse(input)
                .findFirst();

        optionalClicked.ifPresent(clicked -> {
            if (!simulate) {
                List<IFocus<?>> focuses = roles.stream()
                        .<IFocus<?>>map(role -> new Focus<>(role, clicked.getTypedIngredient()))
                        .toList();

                JeiRecipeHistoryPlugin.historyGrid.addHistory(clicked.getTypedIngredient());

                recipesGui.show(focuses);
            }
        });

        return optionalClicked.map(clicked -> LimitedAreaInputHandler.create(this, clicked.getArea()));
    }

}