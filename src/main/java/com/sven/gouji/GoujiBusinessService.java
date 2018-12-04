package com.sven.gouji;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoujiBusinessService {

    //扑克牌列表,利用下标做值用于排序
    private static String[] CardNumber = {"5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2", "大", "小", "3", "4"};

    //用户信息字典
    private static Map<String, Player> UserMap = new HashMap<>();

    //用户扑克队列
    public static Map<String, List<Card>> UserCardsMap = new HashMap<>();

    //用户出牌列表
    public static Map<String, List<Card>> UserDesktopMap = new HashMap<>();

    //扑克-用户对应列表，用于反查收牌时的冲突
    private static Map<Integer, String> CardUserMap = new HashMap<>();

    //扑克-用户桌面对应表，从别人那取牌时需要更新对方的列表
    private static Map<Integer, String> CardUserDeskMap = new HashMap<>();

    //所有扑克列表，用于根据ID反查
    private static Map<Integer, Card> CardsMap = new HashMap<>(216);

    private List<Card> desktop = new ArrayList<>();

    public List<Room> rooms = new ArrayList<>();


    public int msgId = 0;

    public List<String> msgList = new ArrayList<>();


    public synchronized int addMsg(String msg){


        synchronized (msgList){

            if (msgList.size() == 0){
                msgList.add("已到达消息顶部");
            }

            msgList.add(msg);
            msgId++;

        }

        return msgId;

    }


    /**
     * 出牌
     *
     * @param cardIds
     * @param userName
     */
    public synchronized List<Card> giveOut(String cardIds, String userName) {

        //将扑克从用户扑克中抽出，并放在desk中
        String[] cardIdArray = cardIds.split(",");

        List<Card> userCards = UserCardsMap.get(userName);

        List<Card> outCards = new ArrayList<Card>();

        String msg = userName + " 出牌: ";

        for (int i = 0; i < cardIdArray.length; i++) {

            int cardId = Integer.parseInt(cardIdArray[i]);

            //从用户扑克列表中找该扑克，并移除
            for (int j = 0; j < userCards.size(); j++) {

                if (userCards.get(j)._id == cardId) {

                    // var splice = userCards.splice(j,1);
                    Card splice = pickoutCard(j, userCards);
                    outCards.add(splice);
                    //放到桌牌列表中
                    UserDesktopMap.get(userName).add(splice);

                    //将用户对应关系移除
                    CardUserMap.put(cardId, null);
                    CardUserDeskMap.put(cardId, userName);

                    msg += " "+ splice._number;

                    //System.out.println("关系移除=" + cardIdArray[i] + ",value=" + CardUserMap.get(cardId));

                    break;
                }
            }

        }

        addMsg(msg);

        return outCards;

    }

    ;


    public synchronized int fetchCards(String userName, String cardIds) {

        String[] cardIdArray = cardIds.split(",");

        int fetchedCount = 0;

        String msg = userName + " 拿回了";

        for (int i = 0; i < cardIdArray.length; i++) {

            Card card = CardsMap.get(Integer.parseInt(cardIdArray[i]));

            //判断该张扑克不在任何人的手中
            if (CardUserMap.get(card._id) == null) {

                //从用户桌面列表中移除
                String deskUserName = CardUserDeskMap.get(card._id);
                List<Card> userDeskCards = UserDesktopMap.get(deskUserName);
                boolean fetched = false;
                for (int j = 0; j < userDeskCards.size(); j++) {
                    if (userDeskCards.get(j)._id == card._id) {
                        pickoutCard(j, userDeskCards);
                        fetched = true;
                        //System.out.println("从" + deskUserName + "收回了一张" + card._number);
                        msg += " "+card._number+"("+deskUserName+")";
                        break;
                    }
                }
                //可能已经被别人拿走了
                if (!fetched) {
                    continue;
                }

                pickInCard(card, UserCardsMap.get(userName));
                fetchedCount++;
                //注册对应关系
                CardUserMap.put(card._id, userName);

                //System.out.println("注册对应关系=" + cardIdArray[i] + ",value=" + CardUserMap.get(cardIdArray[i]));
                //System.out.println(">>注册对应关系=" + card._id + ",value=" + CardUserMap.get(card._id));

            }

        }
        addMsg(msg);

        return fetchedCount;
    }


    public synchronized int distributeCards(Room room) {

        List<Card> cards = createCards();

        while (cards.size() > 0) {

            for (int i = 0; i < room._players.size() && cards.size() > 0; i++) {

                int index = new Random().nextInt(cards.size());

                Card card = cards.remove(index);

                pickInCard(card, UserCardsMap.get(room._players.get(i)._name));

            }

        }

        room._hasStarted = true;

        return 1;
    }


    public List<Card> createCards() {

        List<Card> cardsList = new ArrayList<>(216);
        int cardId = 1;

        for (int i = 0; i < 11; i++) {
            //处理5-2
            for (int j = 0; j < 16; j++) {
                Card card = new Card(cardId, 5 + i, j + "", CardNumber[i]);
                cardsList.add(card);
                CardsMap.put(cardId, card);
                cardId++;
            }
        }
        for (int i = 11; i < 13; i++) {
            //处理Joker
            for (int j = 0; j < 4; j++) {
                Card card = new Card(cardId, 5 + i, j + "", CardNumber[i]);
                cardsList.add(card);
                CardsMap.put(cardId, card);
                cardId++;
            }
        }

        for (int i = 13; i < 15; i++) {
            //处理3、4
            for (int j = 0; j < 6; j++) {
                Card card = new Card(cardId, 5 + i, j + "", CardNumber[i]);
                cardsList.add(card);
                CardsMap.put(cardId, card);
                cardId++;
            }
        }

        return cardsList;
    }

    public synchronized Card pickoutCard(int index, List<Card> cards) {

        return cards.remove(index);

    }

    /**
     * 插入一张扑克
     *
     * @param card
     * @param cards
     * @return 插入后的下标
     */
    public synchronized int pickInCard(Card card, List<Card> cards) {

        for (int i = 0; i < cards.size(); i++) {
            if (card.value >= cards.get(i).value) {
                cards.add(i, card);
                return i;
            }
        }
        cards.add(card);
        return cards.size() - 1;
    }


    public void createRoom(String name, String pass) {

        Room room = new Room(name,pass);
        rooms.add(room);
    }

    public synchronized Room enterRoom(String name, String pass, String userName) {

        for(int i = 0;i<rooms.size();i++){
            Room room = rooms.get(i);
            if (room._name.equals(name) && room._pass.equals(pass)){

                if (UserMap.get(userName) != null){
                    //TODO已经进入过房间，但没判断是不是当前房间
                    return room;
                }

                //限制人数
                if(room._players.size() ==6){
                    return null;
                }

                Player player = new Player(userName);

                //名称不能重复，记录到字典中
                UserMap.put(userName, player);
                UserCardsMap.put(userName,new ArrayList<Card>());
                UserDesktopMap.put(userName,new ArrayList<Card>());

                room._players.add(player);

                return room;
            }
        }
        return null;
    }

}

class Card {
    public int _id;
    public int value;
    public String _type;
    public String _number;


    public Card(int id, int value, String _type, String _number) {
        this._id = id;
        this.value = value;
        this._type = _type;
        this._number = _number;
    }


}

class Player {
    public String _name;

    public Player(String _name) {
        this._name = _name;
    }
}

class Room {
    public String _name;
    public String _pass;
    public boolean _hasStarted;
    public List<Player> _players = new ArrayList<Player>(6);
    public int playersCount = 0;
    public int _version = 0;

    public Room(String _name, String _pass) {
        this._name = _name;
        this._pass = _pass;
    }
}
