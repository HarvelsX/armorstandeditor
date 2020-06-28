package me.petomka.armorstandeditor.util;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Stream;

public class Menu {

	@Getter
	private static final String INDENT_SEQUENCE = "\u00BB";

	@Getter
	@Setter
	private ClickEvent.Action action = null;

	@Getter
	@Setter
	private String actionString = "";

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private String description;

	private List<Menu> subMenus = Lists.newArrayList();

	public Menu(String message) {
		this.message = message;
	}

	public Menu(String message, ClickEvent.Action action, String actionString) {
		this.message = message;
		this.action = action;
		this.actionString = actionString;
	}

	public Menu(String message, String description) {
		this.message = message;
		this.description = description;
	}

	public Menu(String message, ClickEvent.Action action, String actionString, String description) {
		this.message = message;
		this.action = action;
		this.actionString = actionString;
		this.description = description;
	}

	public List<BaseComponent[]> toComponents() {
		return toComponents(message, 0);
	}

	public List<BaseComponent[]> toComponents(String message, int indent) {
		List<BaseComponent[]> components = Lists.newArrayList();
		BaseComponent[] localComponents = TextComponent.fromLegacyText(message);
		if (action != null) {
			Stream.of(localComponents).forEach(component ->
					component.setClickEvent(new ClickEvent(action, actionString)));
		}
		if (description != null) {
			Stream.of(localComponents).forEach(component ->
					component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							TextComponent.fromLegacyText(description))));
		}
		components.add(localComponents);
		subMenus.forEach(subMenu -> components.addAll(subMenu.toComponents(indent + 1)));
		return components;
	}

	public List<BaseComponent[]> toComponents(int indentation) {
		return toComponents(indentation(indentation).concat(message), indentation);
	}

	public Menu addSub(Menu menu) {
		if (menu.equals(this)) {
			return this;
		}
		subMenus.add(menu);
		return this;
	}

	public boolean removeSub(Menu menu) {
		return subMenus.remove(menu);
	}

	public void clearSubs() {
		subMenus.clear();
	}

	public void send(CommandSender sender) {
		CommandSender.Spigot spigot = sender.spigot();
		toComponents().forEach(spigot::sendMessage);
	}

	private String indentation(int ind) {
		StringBuilder stringBuilder = new StringBuilder(" ");
		while (ind-- > 1) {
			stringBuilder.append(" ");
		}
		stringBuilder.append(INDENT_SEQUENCE).append("  ");
		return stringBuilder.toString();
	}

}
