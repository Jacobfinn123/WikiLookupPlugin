package com.RightClickWiki;
import javax.inject.Inject;
import java.util.Arrays;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.*;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;
import net.runelite.client.util.LinkBrowser;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
@PluginDescriptor(
		name = "Right Click Wiki Lookup",
		description = "Adds a wiki lookup menu option to any applicable right click."
)
public class RightClickWikiPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	public OkHttpClient okHttpClient;
	// Currently just adding the option to NPCs, Game Objects, Ground Items, and CC MenuActions
	private static final Set<MenuAction> allowedMenuActions = ImmutableSet.of(
			MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.EXAMINE_NPC,
			MenuAction.GAME_OBJECT_FIRST_OPTION, MenuAction.GAME_OBJECT_SECOND_OPTION, MenuAction.GAME_OBJECT_THIRD_OPTION, MenuAction.GAME_OBJECT_FOURTH_OPTION, MenuAction.GAME_OBJECT_FIFTH_OPTION, MenuAction.EXAMINE_OBJECT,
			MenuAction.GROUND_ITEM_FIRST_OPTION, MenuAction.GROUND_ITEM_SECOND_OPTION, MenuAction.GROUND_ITEM_THIRD_OPTION, MenuAction.GROUND_ITEM_FOURTH_OPTION, MenuAction.GROUND_ITEM_FIFTH_OPTION, MenuAction.EXAMINE_ITEM_GROUND,
			MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY, MenuAction.WIDGET_TARGET
	);

	/**
	 * Gets the useful text from the menuEntry's target
	 * @param input getTarget()
	 * @return
	 */
	private String extractText(String input){
		Pattern pattern = Pattern.compile(">([^<]*)");  // Gets text between >< or > til end
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}
	private boolean isAllowedMenuAction(MenuAction menuAction){
		return allowedMenuActions.contains(menuAction);
	}

	/**
	 * Insert option
	 */
	@Subscribe
	public void onMenuOpened(MenuOpened event) {
		MenuEntry[] menuEntries = event.getMenuEntries();

		boolean targetFound = false;
		String targetName = "";

		for (MenuEntry menuEntry : menuEntries) {

			if(isAllowedMenuAction(menuEntry.getType()) && !Objects.equals(extractText(menuEntry.getTarget()), "")){
				targetName = extractText(menuEntry.getTarget());
				targetFound = true;
			}
		}

		if (targetFound) {
			MenuEntry entryToAppendOn = menuEntries[menuEntries.length - 1];

			int idx = Arrays.asList(menuEntries).indexOf(entryToAppendOn);

			String finalTargetName = targetName.replace(" ", "_");

			client
					.createMenuEntry(idx - 1)
					.setOption("Lookup Wiki")
					.setTarget(entryToAppendOn.getTarget())
					.setIdentifier(entryToAppendOn.getIdentifier())
					.setParam1(entryToAppendOn.getParam1())
					.setType(MenuAction.of(MenuAction.RUNELITE.getId()))
					.onClick(
							evt -> {
								LinkBrowser.browse("https://oldschool.runescape.wiki/w/" + finalTargetName);
							});
		}
	}

}