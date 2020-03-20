// Copyright 2020 Gabor Kokeny and contributors
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.vaadin.addon.leaflet4vaadin.demo.components;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent;

public abstract class ExampleContainer extends VerticalLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = -731433552726627896L;
	private Tabs tabs = new Tabs();
	private Map<Tab, Component> tabsToPages = new HashMap<>();
	private Set<Component> pagesShown;
	private final String gitHubDir = "https://raw.githubusercontent.com/Gubancs/leaflet4vaadin/master/src/test/java/";

	protected ExampleContainer() {
		super();
		setSizeFull();

		tabs = new Tabs();
		tabs.setWidthFull();

		Tab demoTab = new Tab("Demo");
		demoTab.setSelected(true);
		Component demoLayout = initDemoLayout();
		tabs.add(demoTab);

		Tab sourceTab = new Tab("Java Source");
		sourceTab.setSelected(false);
		Div sourceContent = new Div();
		sourceContent.setSizeFull();
		initSourceContent(sourceContent);
		sourceContent.setVisible(false);
		tabs.add(sourceTab);

		pagesShown = Stream.of(demoLayout).collect(Collectors.toSet());
		tabsToPages.put(demoTab, demoLayout);
		tabsToPages.put(sourceTab, sourceContent);

		tabs.addSelectedChangeListener(onPageChange());

		add(tabs, demoLayout, sourceContent);
	}

	private ComponentEventListener<SelectedChangeEvent> onPageChange() {
		return event -> {
			pagesShown.forEach(page -> page.setVisible(false));
			pagesShown.clear();
			Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
			selectedPage.setVisible(true);
			pagesShown.add(selectedPage);
		};
	}

	protected void initApiContent(HasComponents apiContent) {
	}

	protected void initSourceContent(HasComponents tabContent) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(false);
		verticalLayout.setPadding(false);
		try {
			Pre code = new Pre(getSourceCode());
			code.getStyle().set("padding", "30px");
			code.getStyle().set("background", "#3a3a3a");
			code.getStyle().set("color", "#fff");
			code.getStyle().set("font-size", "14px");
			code.getStyle().set("-webkit-font-smoothing", "antialiased");
			verticalLayout.add(code);
		} catch (Exception e) {
			verticalLayout.add(new Label("Unable to get source code from GitHub. :("));
			e.printStackTrace();
		}
		tabContent.add(verticalLayout);
	}

	protected Component initDemoLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		Div demoContent = new Div();
		demoContent.setSizeFull();
		initMap(demoContent);
		layout.add(demoContent);
		return layout;
	}

	protected abstract void initMap(Div mapContainer);

	private String getSourceCode() throws Exception {
		URL website = new URL(getGitHubPath());
		URLConnection connection = website.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			if (inputLine.startsWith("import ") || inputLine.startsWith("// ") || inputLine.startsWith("package ")) {
				continue;
			}
			response.append(inputLine);
			response.append("\n");
		}

		in.close();

		return response.toString().trim();
	}

	private String getGitHubPath() {
		return gitHubDir + getClass().getName().replaceAll("\\.", "/") + ".java";
	}
}
