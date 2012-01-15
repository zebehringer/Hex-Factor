package zeb.catan.google.app.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.vectomatic.dom.svg.OMElement;
import org.vectomatic.dom.svg.OMNode;
import org.vectomatic.dom.svg.OMSVGAElement;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.AsyncXmlLoader;
import org.vectomatic.dom.svg.utils.AsyncXmlLoaderCallback;
import org.vectomatic.dom.svg.utils.DOMHelper;
import org.vectomatic.dom.svg.utils.SVGPrefixResolver;

import catan.Catan.Product;
import catan.Catan.ResourceCount;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class GameBoard extends Composite {

	private GameHostServiceAsync gameLobbyService;
	private String gameKey;
	private SelfPlayer selfPlayer;
    private PlayerInfo currentPlayer;
	private StateHandler currentStateHandler;
	private SVGPrefixResolver svgPrefixResolver;

	OMSVGDocument document;
    /**
     * Root element of the board SVG document
     */
    OMSVGSVGElement svgRoot;
    
    AsyncXmlLoader loader;

    FlowPanel topPanel;
    CaptionPanel playersPanel;
    PlayerCell playerCell;
    CellList<PlayerInfo> playersList;
    FlowPanel mainPanel;
    //FlowPanel actionsPanel;
    ButtonCell actionCell;
    CellList<String> actionsPanel;
    ResourceCountCell resourceCountCell;
    CellList<ResourceCount> resourceCountsPanel;
    FlowPanel eastPanel;
    HTML chatLog;
    ScrollPanel chatLogScroller;
    DockLayoutPanel chatEntryPanel;
    TextBox chatMessageText;
    

	public GameBoard(final GameHostServiceAsync gameLobbyService) {
		this.gameLobbyService = gameLobbyService;
		this.svgPrefixResolver = new SVGPrefixResolver();

		loader = GWT.create(AsyncXmlLoader.class);

		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		
		topPanel = new FlowPanel();
		dockLayoutPanel.addNorth(topPanel, 2);
		
		chatEntryPanel = new DockLayoutPanel(Unit.EM);
		
		Button sendMessageButton = new Button();
		sendMessageButton.setText("Send");
		sendMessageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendChatMessage();
			}
		});
		chatEntryPanel.addEast(sendMessageButton, 5);
		
		chatMessageText = new TextBox();
		chatMessageText.setMaxLength(100);
		chatMessageText.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == '\r') {
					sendChatMessage();
				}
			}
		});
		chatMessageText.setWidth("100%");
		chatEntryPanel.add(chatMessageText);
		
		dockLayoutPanel.addSouth(chatEntryPanel, 2);

		chatLog = new HTML("", true);
		chatLogScroller = new ScrollPanel();
		chatLogScroller.setSize("100%","90%");
		chatLogScroller.add(chatLog);
		CaptionPanel chatPanel = new CaptionPanel("Chat Log");
		chatPanel.add(chatLogScroller);
		dockLayoutPanel.addSouth(chatPanel, 10);

		playersPanel = new CaptionPanel("Players");
		playerCell = new PlayerCell();
		playersList = new CellList<PlayerInfo>(playerCell);
		playersPanel.add(playersList);
		dockLayoutPanel.addWest(playersPanel, 15);
		
		actionCell = new ActionButtonCell();
		CaptionPanel actionsCaptionPanel = new CaptionPanel("Actions");
		actionsCaptionPanel.setSize("92%","49%");
		actionsPanel = new CellList<String>(actionCell);
		CaptionPanel resourcesCaptionPanel = new CaptionPanel("Your Resources");
		resourcesCaptionPanel.setSize("92%","49%");
		resourceCountCell = new ResourceCountCell();
		resourceCountsPanel = new CellList<ResourceCount>(resourceCountCell);
		actionsCaptionPanel.add(actionsPanel);
		resourcesCaptionPanel.add(resourceCountsPanel);
		eastPanel = new FlowPanel();
		eastPanel.setSize("94%", "96%");
		eastPanel.add(actionsCaptionPanel);
		eastPanel.add(resourcesCaptionPanel);
		dockLayoutPanel.addEast(eastPanel, 15);
		
		mainPanel = new FlowPanel();
		dockLayoutPanel.add(mainPanel);
		
		Button leaveGameButton = new Button();
		leaveGameButton.setText("leave game");
		leaveGameButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				gameLobbyService.leaveGame(new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						Window.alert("logout failed");
					}
					public void onSuccess(Void v) {
						Element div = mainPanel.getElement();
						div.removeChild(svgRoot.getElement());
						svgRoot = null;
					}
				});
			}
		});
		topPanel.add(leaveGameButton);
		
		initWidget(dockLayoutPanel);
	}
	
	private class ActionButtonCell extends ButtonCell {
		public ActionButtonCell() {
			super();
		}
		@Override
		public void onBrowserEvent(Context context, Element parent,
				String value, NativeEvent event,
				ValueUpdater<String> valueUpdater) {
			switch (DOM.eventGetType((Event)event)) {
            case Event.ONCLICK:
            	
                currentStateHandler.beginAction(value);
                break;
			}
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}
	
	private void sendChatMessage() {
		if (chatMessageText.getText() == null || chatMessageText.getText().trim().length() == 0) {
			return;
		}
		String message = chatMessageText.getText();
		if (message.length() > 100) {
			message = message.substring(0,100);
		}
		gameLobbyService.sendMessage(message, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				appendSystemMessage("send message failed");
			}
			@Override
			public void onSuccess(Void result) {
				chatMessageText.setText("");
			}
		});
	}
	
	protected void appendSystemMessage(String message) {
		chatLog.setHTML(chatLog.getHTML()+"<p class='sys'>" + message + "</p>");
		chatLogScroller.scrollToBottom();
	}
	
	protected void feedback(String message) {
		
	}
	
	protected void warning(String message) {
		chatLog.setHTML(chatLog.getHTML()+"<p class='warn'>" + message + "</p>");
		chatLogScroller.scrollToBottom();
	}
	
	protected void error(String message) {
		
	}
	
	private HashMap<String,ChannelMessageHandler> messageHandlers;
	
	private void routeMessage(String message) {
		if (message.indexOf("::") >= 0) {
			handleMessage(message);
		}
		else {
			appendSystemMessage("general message: " + message);
		}
	}
	
	private class PlayerProvisioned implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			selfPlayer = toPlayer(payload).get(0);
			resourceCountsPanel.setRowData(selfPlayer.listResourceCounts());
			//messageHandlers.remove("player provisioned");
		}
	}
	
	private class GameCreated implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			appendSystemMessage("game created");
    		GameCreatedEvent event = toGameCreatedEvent(payload).get(0);
    		players = new ArrayList<PlayerInfo>();
    		for (int i=0; i<event.getPlayers().length(); i++) {
    			players.add(event.getPlayers().get(i));
    		}
    		loadBoard();
    		playersList.setRowData(players);
    		//messageHandlers.remove("game created");
		}
		
		private final native JsArray<GameCreatedEvent> toGameCreatedEvent(String json) /*-{
			return eval(json);
		}-*/;
	}
	
	private class StateChanged implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			stateChanged(toStateChangeEvent(payload));
		}
		private final native JsArray<StateChangeEventJso> toStateChangeEvent(String json) /*-{
			return eval(json);
		}-*/;
	}
	
	private class ProductBuilt implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			//productBuilt(payload);
			JsArray<ProductBuiltEvent> events = toEvents(payload);
			for (int i=0; i<events.length(); i++) {
				ProductBuiltEvent event = events.get(i);
				switch (event.getProduct()) {
					case ROAD:
						roadBuilt(findPlayer(event.getPlayerId()),event.getSiteIds());
						break;
					case SETTLEMENT:
					case CITY:
						buildingBuilt(event.getProduct(),findPlayer(event.getPlayerId()),(int)event.getSiteIds().get(0));
						break;
				}
			}
		}
		private final native JsArray<ProductBuiltEvent> toEvents(String json) /*-{
			return eval(json);
		}-*/;
	}
	
	private class ResourcesEarned implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			JsArray<ResourcesEarnedEvent> events = toEvents(payload);
			for (int i=0; i<events.length(); i++) {
				ResourcesEarnedEvent evt = events.get(i);
				if (evt.getPlayerId().equals(selfPlayer.getUniqueId())) {
					selfPlayer.addResources(evt.getTypeId(),evt.getCount());
				}
				else {
					//PlayerInfo player = findPlayer(evt.getPlayerId());
					//player.addResources(evt);
				}
			}
			resourceCountsPanel.setRowData(selfPlayer.listResourceCounts());
		}
		private final native JsArray<ResourcesEarnedEvent> toEvents(String json) /*-{
			return eval(json);
		}-*/;
	}
	
	private class ResourcesExchanged implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			
		}
	}
	
	private class ResourcesTaken implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			
		}
	}
	
	private class ChatReceived implements ChannelMessageHandler {
		@Override
		public void handleMessage(GameBoard board, String payload) {
			chatLog.setHTML(chatLog.getHTML()+"<p class='chat'>" + payload.trim() + "</p>");
			chatLogScroller.scrollToBottom();
		}
	}
	
	private void handleMessage(String message) {
		String key = message.substring(0,message.indexOf("::"));
		ChannelMessageHandler handler = messageHandlers.get(key);
		if (handler == null) {
			appendSystemMessage("unhandled message: " + message);
		}
		else {
			//appendSystemMessage(message);
			handler.handleMessage(this, message.substring(message.indexOf("::")+2));
		}
	}
	
	private List<PlayerInfo> players;
	
	private PlayerInfo findPlayer(String uniqueId) {
		for (PlayerInfo player : players) {
			if (player.getUniqueId().equals(uniqueId)) {
				return player;
			}
		}
		return null;
	}
	
	private final native JsArray<SelfPlayer> toPlayer(String json) /*-{
		return eval(json);
	}-*/;

	public String getGameKey() {
		return gameKey;
	}

	public void connect(String channelToken, String gameKey) {
		this.gameKey = gameKey;
		messageHandlers = new HashMap<String,ChannelMessageHandler>();
		messageHandlers.put("player provisioned", new PlayerProvisioned());
		messageHandlers.put("game created", new GameCreated());
		messageHandlers.put("state changed", new StateChanged());
		messageHandlers.put("product built", new ProductBuilt());
		messageHandlers.put("resources earned", new ResourcesEarned());
		messageHandlers.put("resources exchanged", new ResourcesExchanged());
		messageHandlers.put("resources taken", new ResourcesTaken());
		messageHandlers.put("chat", new ChatReceived());
		ChannelFactory.createChannel(channelToken, new ChannelCreatedCallback() {
			@Override
			public void onChannelCreated(Channel channel) {
				channel.open(new SocketListener() {
					@Override
                    public void onOpen() {
						gameLobbyService.playerReady(new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert("player ready failed");
							}
							@Override
							public void onSuccess(Void v) {
								appendSystemMessage("player ready");
							}
						});
                        appendSystemMessage("Channel opened");
                    }
                    @Override
                    public void onMessage(String message) {
                    	routeMessage(message);
                    }
                    @Override
                    public void onClose() {
                    	appendSystemMessage("Channel closed");
                    }
                    @Override
                    public void onError(SocketError error) {
                    	appendSystemMessage("Channel error " + error.getDescription());
                    }
				});
			}
		});
	}

	public void loadBoard() {
		String url = "/play/board.svg?width=600&height=600";
        loader.loadResource(url, new AsyncXmlLoaderCallback() {
                @Override
                public void onError(String resourceName, Throwable error) {
                    mainPanel.getElement().appendChild(Document.get().createTextNode("Cannot find resource"));
                }

                @Override
                public void onSuccess(String resourceName, com.google.gwt.dom.client.Element root) {
                    OMSVGSVGElement svg = OMNode.convert(root);
                    displayBoard(svg);
                    gameLobbyService.gameClientReady(getGenericCallback());
                }
        });
	}
	
	private void displayBoard(OMSVGSVGElement svg) {
	    // Add the SVG to the HTML page
		Element div = mainPanel.getElement();
		if (svgRoot != null) {
			div.replaceChild(svg.getElement(), svgRoot.getElement());
		} else {
			div.appendChild(svg.getElement());                                      
		}
		svgRoot = svg;
		document = (OMSVGDocument) svgRoot.getOwnerDocument();  

		for (OMElement anchor : getSVGElementById("tiles").getElementsByTagName("a")) {
			((OMSVGAElement)anchor).addClickHandler(tileClickHandler);
		}
		for (OMElement anchor : getSVGElementById("sites").getElementsByTagName("a")) {
			((OMSVGAElement)anchor).addClickHandler(siteClickHandler);
		}
		for (OMElement line : getSVGElementById("roads").getElementsByTagName("line")) {
			((OMSVGLineElement)line).addClickHandler(roadClickHandler);
		}
	}
	
	public void setPickTile() {
		((OMSVGElement)getSVGElementById("sites")).setClassNameBaseVal("");
		((OMSVGElement)getSVGElementById("roads")).setClassNameBaseVal("");
		((OMSVGElement)getSVGElementById("tiles")).addClassNameBaseVal("picking_tile");
		clearRoadSuggestion();
	}
	
	public void setPickSite(int siteType) {
		((OMSVGElement)getSVGElementById("tiles")).removeClassNameBaseVal("picking");
		((OMSVGElement)getSVGElementById("roads")).setClassNameBaseVal("");
		((OMSVGElement)getSVGElementById("sites")).setClassNameBaseVal("player"+selfPlayer.getNumber()+"_picking");
		((OMSVGElement)getSVGElementById("sites")).addClassNameBaseVal("pickingType"+siteType);
		clearRoadSuggestion();
	}
	
	public void setPickRoad() {
		((OMSVGElement)getSVGElementById("sites")).setClassNameBaseVal("");
		((OMSVGElement)getSVGElementById("sites")).removeClassNameBaseVal("picking");
		((OMSVGElement)getSVGElementById("roads")).setClassNameBaseVal("player"+selfPlayer.getNumber()+"_picking");
		clearRoadSuggestion();
	}
	
	public void setPickNone() {
		((OMSVGElement)getSVGElementById("sites")).setClassNameBaseVal("");
		((OMSVGElement)getSVGElementById("sites")).removeClassNameBaseVal("picking");
		((OMSVGElement)getSVGElementById("roads")).setClassNameBaseVal("");
		clearRoadSuggestion();
	}
	
	public void suggestRoads(String atSiteId) {
		for (OMElement line : ((OMSVGElement)getSVGElementById("roads")).getElementsByTagName("line")) {
			String[] parts = line.getId().split("_");
			if (parts[0].equals(atSiteId) || parts[1].equals(atSiteId)) {
				OMSVGElement svgLine = (OMSVGElement)line;
				if (!svgLine.getClassName().getBaseVal().contains("reality")) {
					svgLine.addClassNameBaseVal("possibility");
				}
			}
		}
	}
	
	public void clearRoadSuggestion() {
		for (OMElement line : ((OMSVGElement)getSVGElementById("roads")).getElementsByTagName("line")) {
			((OMSVGElement)line).removeClassNameBaseVal("possibility");
		}
	}
	
	public void roadBuilt(PlayerInfo player, JsArrayNumber betweenSiteIds) {
		int a = (int)betweenSiteIds.get(0);
		int b = (int)betweenSiteIds.get(1);
		OMSVGElement line = (OMSVGElement)getSVGElementById(a+"_"+b);
		line.addClassNameBaseVal("reality player"+player.getNumber());
	}
	
	public void buildingBuilt(Product product, PlayerInfo player, int siteId) {
		OMSVGElement site = (OMSVGElement)getSVGElementById("site"+siteId);
		site.setClassNameBaseVal("player"+player.getNumber());
		site.addClassNameBaseVal(product.name().toLowerCase());
	}
	
	protected OMElement getSVGElementById(String id) {
		Iterator<OMElement> it = DOMHelper.evaluateXPath(svgRoot, ".//*[@id='"+id+"']", svgPrefixResolver);
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}

	public SelfPlayer getSelfPlayer() {
		return selfPlayer;
	}

	public PlayerInfo getCurrentPlayer() {
		return currentPlayer;
	}

	private AsyncCallback<Void> genericCallback;

	public AsyncCallback<Void> getGenericCallback() {
		if (genericCallback == null) {
			genericCallback = new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					appendSystemMessage("send message failed");
				}
				@Override
				public void onSuccess(Void result) {
					
				}
			};
		}
		return genericCallback;
	}
	
	public AsyncCallback<Integer> getRollOutcomeCallback() {
		return new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				appendSystemMessage("send message failed");
			}
			@Override
			public void onSuccess(Integer result) {
				Window.alert("a " + result + " was rolled");
			}
		};
	}

	private SetupStateServiceAsync setupStateService;

	public SetupStateServiceAsync getSetupStateService() {
		if (setupStateService == null) {
			setupStateService = (SetupStateServiceAsync) GWT.create(SetupStateService.class);
		}
		return setupStateService;
	}
	
	private WaitingForTurnStateServiceAsync waitingForTurnService;

	public WaitingForTurnStateServiceAsync getWaitingForTurnStateService() {
		if (waitingForTurnService == null) {
			waitingForTurnService = (WaitingForTurnStateServiceAsync) GWT.create(WaitingForTurnStateService.class);
		}
		return waitingForTurnService;
	}
	
	private RobberStateServiceAsync robberStateService;

	public RobberStateServiceAsync getRobberStateService() {
		if (robberStateService == null) {
			robberStateService = (RobberStateServiceAsync) GWT.create(RobberStateService.class);
		}
		return robberStateService;
	}
	
	private WaitingForYieldStateServiceAsync waitingForYieldService;

	public WaitingForYieldStateServiceAsync getWaitingForYieldStateService() {
		if (waitingForYieldService == null) {
			waitingForYieldService = (WaitingForYieldStateServiceAsync) GWT.create(WaitingForYieldStateService.class);
		}
		return waitingForYieldService;
	}
	
	private TradeStateServiceAsync tradeStateService;

	public TradeStateServiceAsync getTradeStateService() {
		if (tradeStateService == null) {
			tradeStateService = (TradeStateServiceAsync) GWT.create(TradeStateService.class);
		}
		return tradeStateService;
	}

	private StateHandler getStateHandler(String state) {
		if (state.equalsIgnoreCase("Setup")) {
			return new SetupStateHandler(this);
		}
		else if (state.equalsIgnoreCase("WaitingForTurn")) {
			return new WaitingForTurnStateHandler(this);
		}
		else if (state.equalsIgnoreCase("Robber")) {
			return new RobberStateHandler(this);
		}
		else if (state.equalsIgnoreCase("WaitingForYield")) {
			return new WaitingForYieldStateHandler(this);
		}
		else if (state.equals("Trading")) {
			return new TradeStateHandler(this);
		}
		return null;
	}

	public void stateChanged(JsArray<StateChangeEventJso> events) {
		StateChangeEventJso event = events.get(0);
		if (this.currentStateHandler != null && this.currentStateHandler.getState().equalsIgnoreCase(event.getOldState())) {
			this.currentStateHandler.stateChanged(event.getCause());
		}
		this.currentPlayer = findPlayer(event.getNewPlayer());
		this.currentStateHandler = getStateHandler(event.getNewState());
		String[] actions = this.currentStateHandler.listActions(this.selfPlayer);
		if (actions != null) {
			if (actions.length == 1 && this.currentStateHandler.isAutoAction(actions[0])) {
				this.currentStateHandler.beginAction(actions[0]);
			}
			else {
				displayNonModalOptions(this.currentStateHandler.getState(), "Select an action", actions);
			}
		}
	}
	
	private OptionSelectCallback currentOptionSelectCallback;

	protected void displayOptions(String title, String heading, Object[] options, OptionSelectCallback callback) {
		currentOptionSelectCallback = callback;
		ArrayList<String> strings = new ArrayList<String>(options.length);
		for (Object option : options) {
			strings.add(option.toString());
		}
		//actionsPanel.setRowData(strings);
	}

	protected void displayNonModalOptions(String state, String title, String[] actions) {
		actionsPanel.setRowData(Arrays.asList(actions));
	}

	private ClickHandler tileClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			event.preventDefault();
			if (currentStateHandler != null) {
				currentStateHandler.tilePick(((OMSVGAElement)event.getSource()).getHref().getBaseVal());
			}
		}
	};

	private ClickHandler siteClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			event.preventDefault();
			if (currentStateHandler != null) {
				currentStateHandler.sitePick(((OMSVGAElement)event.getSource()).getHref().getBaseVal());
			}
		}
	};
	
	private ClickHandler roadClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			event.preventDefault();
			if (currentStateHandler != null) {
				String[] siteIds = ((OMSVGElement)event.getSource()).getId().split("_");
				currentStateHandler.roadPick(siteIds[0], siteIds[1]);
			}
		}
	};
}
