package com.sven.gouji;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GoujiController {

    @Autowired
    private GoujiBusinessService goujiBusinessService;

    @RequestMapping("/api/rooms")
    public Object rooms() {
        return goujiBusinessService.rooms;

    }

    @RequestMapping("/api/createRoom")
    public Object createRoom(String name,String pass) {


        goujiBusinessService.createRoom(name,pass);

       return goujiBusinessService.rooms;

    }

    @RequestMapping("/api/enterRoom")
    public Object enterRoom(String name,String pass,String userName) {

        Room room = goujiBusinessService.enterRoom(name,pass,userName);

        if (room != null){
            String msg = userName + " 进入了房间";
            goujiBusinessService.addMsg(msg);
            room._version ++;
        }

        return room;

    }

    @RequestMapping("/api/start")
    public Object start(String userName) {

        //默认当前房间为第一个房间
        Room room = goujiBusinessService.rooms.get(0);

        if(!room._hasStarted ){//游戏没有开始

            if(room._players.size() <6){
                goujiBusinessService.addMsg("人数不足");
                return "{}";
            }

            goujiBusinessService.distributeCards(room);

        }

        List<Card> cards = goujiBusinessService.UserCardsMap.get(userName);
        List<Card>  deskCards = goujiBusinessService.UserDesktopMap.get(userName);
        Map<String,Object> map = new HashMap<>();
        map.put("cards",cards);
        map.put("deskCards",deskCards);

        room._version ++;

        return map;

    }

    @RequestMapping("/api/attack")
    public Object attack(String userName,String cardIds) {

        //默认当前房间为第一个房间
        Room room = goujiBusinessService.rooms.get(0);

        goujiBusinessService.giveOut(cardIds, userName);

        List<Card> cards = goujiBusinessService.UserCardsMap.get(userName);
        List<Card>  deskCards = goujiBusinessService.UserDesktopMap.get(userName);
        Map<String,Object> map = new HashMap<>();
        map.put("cards",cards);
        map.put("deskCards",deskCards);

        room._version ++;

        return map;

    }

    @RequestMapping("/api/fetchBack")
    public Object fetchBack(String userName,String cardIds) {

        //默认当前房间为第一个房间
        Room room = goujiBusinessService.rooms.get(0);

        goujiBusinessService.fetchCards(userName,cardIds);

        List<Card> cards = goujiBusinessService.UserCardsMap.get(userName);
        List<Card>  deskCards = goujiBusinessService.UserDesktopMap.get(userName);
        Map<String,Object> map = new HashMap<>();
        map.put("cards",cards);
        map.put("deskCards",deskCards);

        room._version ++;
        return map;
    }

    @RequestMapping("/api/pass")
    public Object pass(String userName) {

        //默认当前房间为第一个房间
        Room room = goujiBusinessService.rooms.get(0);

        if(!room._hasStarted ){//游戏没有开始

           return "{}";

        }

        goujiBusinessService.UserDesktopMap.get(userName).clear();

        List<Card> cards = goujiBusinessService.UserCardsMap.get(userName);
        List<Card>  deskCards = goujiBusinessService.UserDesktopMap.get(userName);
        Map<String,Object> map = new HashMap<>();
        map.put("cards",cards);
        map.put("deskCards",deskCards);

        room._version ++;
        return map;
    }

    @RequestMapping("/api/loopCheck")
    public Object loopCheck(int version) {

        if (goujiBusinessService.rooms.size() == 0)
            return  "{}";

        //默认当前房间为第一个房间
        Room room = goujiBusinessService.rooms.get(0);

        if (!room._hasStarted && room._version < version) {
            Map<String,Object> map = new HashMap<>();
            map.put("version",room._version);
            map.put("room",room);

            return map;
        }

        //版本没有变化
        if(room._version <= version){

            return "{}";
        }


        Map<String,Object> map = new HashMap<>();
        map.put("version",room._version);
        map.put("room",room);
        map.put("UserDesktopMap",goujiBusinessService.UserDesktopMap);

        return map;
    }

    @RequestMapping("/api/sendMsg")
    public Object sendMsg(String userName,String msg) {

        if (msg.contains("<") || msg.contains(">") || msg.contains("/") ||msg.contains("\\")){
            return "{}";
        }

        goujiBusinessService.addMsg(userName+" 说 "+msg);

        return "{}";

    }


    @RequestMapping("/api/msg")
    public Object msg(int msgId) {

        if (msgId < goujiBusinessService.msgId){

            Map<String,Object> map = new HashMap<>();

            List<String> msgList =  goujiBusinessService.msgList.subList(msgId+1,goujiBusinessService.msgList.size());

            map.put("msgId" , goujiBusinessService.msgId);
            map.put("msgList" , msgList);

            return map;

        }


        return "{}";

    }







//    @RequestMapping("/api/rooms")
//    public Object rooms(@RequestParam(value = "name", defaultValue = "World") String name) {
//
//        Map<String, Object> res = new HashMap<>();
//
//        res.put("success", true);
//        res.put("players", 2);
//        res.put("cards", new int[]{8, 9});
//
//        return res;
//
//    }

}
