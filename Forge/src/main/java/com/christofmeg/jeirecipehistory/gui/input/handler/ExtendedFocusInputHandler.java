package com.christofmeg.jeirecipehistory.gui.input.handler;

import com.christofmeg.jeirecipehistory.config.JeiRecipeHistoryConfig;
import com.christofmeg.jeirecipehistory.gui.jei.JeiRecipeHistoryPlugin;
import com.christofmeg.jeirecipehistory.recipe.IRecipeInfo;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.library.focus.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import mezz.jei.gui.input.handlers.LimitedAreaInputHandler;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
* Based on {@link FocusInputHandler}, but with some modifications to facilitate reading recipes from {@link IRecipeInfo}.
*/
@SuppressWarnings("unused")
public class ExtendedFocusInputHandler implements IUserInputHandler {

private static final List<RecipeIngredientRole> SHOW_RECIPE_ROLES = List.of(RecipeIngredientRole.OUTPUT);
private static final List<RecipeIngredientRole> SHOW_USES_ROLES = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST);
private final CombinedRecipeFocusSource focusSource;
private final RecipesGui recipesGui;
private final IFocusFactory focusFactory;

    /**
    * Adds items to the recipe history list
    */
    @Contract("_, _ -> new")
    @SuppressWarnings("unused")
    public static @NotNull IUserInputHandler create(CombinedRecipeFocusSource focusSource, RecipesGui recipesGui, IFocusFactory focusFactory) {
        if(JeiRecipeHistoryConfig.isAllModFeatuesDisabled()) {
            return new FocusInputHandler(focusSource, recipesGui, focusFactory);
        }
        else {
            return new ExtendedFocusInputHandler(focusSource, recipesGui, focusFactory);
        }
    }

    public ExtendedFocusInputHandler(CombinedRecipeFocusSource focusSource, RecipesGui recipesGui, IFocusFactory focusFactory) {
        this.focusSource = focusSource;
        this.recipesGui = recipesGui;
        this.focusFactory = focusFactory;
    }

    @Override
    public @NotNull Optional<IUserInputHandler> handleUserInput(@NotNull Screen screen, @NotNull UserInput input, @NotNull IInternalKeyMappings keyBindings) {
        return handleOriginalShow(input, keyBindings);
    }

    private Optional<IUserInputHandler> handleOriginalShow(@NotNull UserInput input, @NotNull IInternalKeyMappings keyBindings) {
        return input.is(keyBindings.getShowRecipe()) ? handleShow(input, keyBindings, SHOW_RECIPE_ROLES)
                : input.is(keyBindings.getShowUses()) ? handleShow(input, keyBindings, SHOW_USES_ROLES)
                : Optional.empty();
    }

    private Optional<IUserInputHandler> handleShow(@NotNull UserInput input, @NotNull IInternalKeyMappings keyBinding, List<RecipeIngredientRole> roles) {
        boolean simulate = input.isSimulate();
        Optional<IClickableIngredientInternal<?>> optionalClicked = focusSource.getIngredientUnderMouse(input, keyBinding)
                .findFirst();

        optionalClicked.ifPresent(clicked -> {
            if (!simulate) {
                List<IFocus<?>> focuses = roles.stream()
                        .<IFocus<?>>map(role -> new Focus<>(role, clicked.getTypedIngredient()))
                        .toList();
                /**
                 * Adds items to recipe history only when history is shown
                 */
                if (JeiRecipeHistoryConfig.isRecipeHistoryEnabled() && !JeiRecipeHistoryConfig.isAllModFeatuesDisabled()) {
                    JeiRecipeHistoryPlugin.historyGrid.addHistory(clicked.getTypedIngredient());
                }
                recipesGui.show(focuses);
            }
        });

    return optionalClicked.map(clicked -> LimitedAreaInputHandler.create(this, clicked.getArea()));
    }
}