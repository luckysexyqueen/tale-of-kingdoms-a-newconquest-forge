package com.convallyria.taleofkingdoms.client.gui.entity;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.gui.ScreenTOK;
import com.convallyria.taleofkingdoms.client.gui.entity.widget.PageTurnWidget;
import com.convallyria.taleofkingdoms.client.gui.entity.widget.ShopButtonWidget;
import com.convallyria.taleofkingdoms.client.gui.entity.widget.ShopScreenInterface;
import com.convallyria.taleofkingdoms.client.gui.image.Image;
import com.convallyria.taleofkingdoms.client.gui.image.ScaleSize;
import com.convallyria.taleofkingdoms.client.gui.shop.Shop;
import com.convallyria.taleofkingdoms.client.gui.shop.ShopPage;
import com.convallyria.taleofkingdoms.client.translation.Translations;
import com.convallyria.taleofkingdoms.client.utils.ShopBuyUtil;
import com.convallyria.taleofkingdoms.common.entity.guild.FoodShopEntity;
import com.convallyria.taleofkingdoms.common.shop.ShopItem;
import com.convallyria.taleofkingdoms.client.schematic.ClientConquestInstance;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FoodShopScreen extends ScreenTOK implements ShopScreenInterface {

    private final PlayerEntity player;
    private final FoodShopEntity entity;
    private final ClientConquestInstance instance;

    private final ImmutableList<ShopItem> shopItems;
    private ShopItem selectedItem;
    private Shop shop;

    private static final ImmutableList<ScaleSize> SCALE_SIZES = ImmutableList.of(
            new ScaleSize(1, 810, 265),
            new ScaleSize(2, 310, 113),
            new ScaleSize(3, 170, 55),
            new ScaleSize(4, 100, 27));

    private static final ImmutableList<ScaleSize> SCALE_SIZES_TWO = ImmutableList.of(
            new ScaleSize(1, 1000, 265),
            new ScaleSize(2, 540, 113),
            new ScaleSize(3, 360, 55),
            new ScaleSize(4, 275, 27));

    public FoodShopScreen(PlayerEntity player, FoodShopEntity entity, ClientConquestInstance instance) {
        super("menu.taleofkingdoms.foodshop.name");
        this.player = player;
        this.entity = entity;
        this.instance = instance;
        this.shopItems = FoodShopEntity.getFoodShopItems();
        int guiScale = MinecraftClient.getInstance().options.guiScale;
        Optional<ScaleSize> scaleSize = SCALE_SIZES.stream().filter(size -> size.getGuiScale() == guiScale).findFirst();
        if (scaleSize.isEmpty()) return;
        int x = scaleSize.get().getX();
        int y = scaleSize.get().getY();
        Optional<ScaleSize> scaleSizeTwo = SCALE_SIZES_TWO.stream().filter(size -> size.getGuiScale() == guiScale).findFirst();
        if (scaleSizeTwo.isEmpty()) return;
        int xTwo = scaleSizeTwo.get().getX();
        int yTwo = scaleSizeTwo.get().getY();
        addImage(new Image(new Identifier(TaleOfKingdoms.MODID, "textures/gui/menu1.png"), x, y, new int[]{230, 230}));
        addImage(new Image(new Identifier(TaleOfKingdoms.MODID, "textures/gui/menu2.png"), xTwo, yTwo, new int[]{230, 230}));
    }

    @Override
    public ShopItem getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(ShopItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public void init() {
        super.init();
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 132 , this.height / 2 - 55, 55, 20, new LiteralText("Buy"), button -> {
            int count = 1;
            if (Screen.hasShiftDown()) count = 16;
            ShopBuyUtil.buyItem(instance, player, selectedItem, count);
        }, (button, stack, x, y) -> {
            Text text = new LiteralText("Use Left Shift to buy 16x.");
            this.renderTooltip(stack, text, x, y);
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 132, this.height / 2 - 30, 55, 20, new LiteralText("Sell"), button -> openSellGui(entity, player)));
        this.addDrawableChild(new PageTurnWidget(this.width / 2 - 135, this.height / 2 - 100, false, button -> shop.previousPage(), true));
        this.addDrawableChild(new PageTurnWidget(this.width / 2 + 130, this.height / 2 - 100, true, button -> shop.nextPage(), true));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 160, this.height / 2 + 20, 45, 20, new LiteralText("Exit"), button -> this.close()));

        this.selectedItem = shopItems.get(0);

        final int maxPerSide = 18;
        int page = 0;
        int currentIteration = 0;
        int currentY = this.height / 4;
        int currentX = this.width / 2 - 100;
        Map<Integer, ShopPage> pages = new HashMap<>();
        pages.put(page, new ShopPage(page));

        for (ShopItem shopItem : shopItems) {
            if (currentIteration == 9) {
                currentY = this.height / 4;
                currentX = currentX + 115;
            }

            if (currentIteration >= maxPerSide) {
                page++;
                currentIteration = 0;
                currentY = this.height / 4;
                currentX = this.width / 2 - 100;
                pages.put(page, new ShopPage(page));
            }

            ShopButtonWidget shopButtonWidget = this.addDrawableChild(new ShopButtonWidget(shopItem, this, currentX, currentY, this.textRenderer));
            pages.get(page).addItem(shopButtonWidget);

            currentY = currentY + 20;
            currentIteration++;
        }

        this.shop = new Shop(pages);
        pages.get(0).show();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);
        drawCenteredText(stack, this.textRenderer, "Shop Menu - Total Money: " + instance.getCoins() + " Gold Coins", this.width / 2, this.height / 4 - 25, 0xFFFFFF);
        if (this.selectedItem != null) {
            drawCenteredText(stack, this.textRenderer, "Selected Item Cost: " + this.selectedItem.getName() + " - " + this.selectedItem.getCost() + " Gold Coins", this.width / 2, this.height / 4 - 15, 0xFFFFFF);
        }
    }

    @Override
    public void close() {
        super.close();
        Translations.SHOP_CLOSE.send(player);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
