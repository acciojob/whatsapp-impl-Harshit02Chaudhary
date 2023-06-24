package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)) {
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        User user = new User(name, mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        User admin = users.get(0);
        String name = "";

        if(users.size() == 2) {
            name = users.get(1).getName();
        }
        else {
            customGroupCount += 1;
            name = "Group" + customGroupCount;
        }

        Group group = new Group(name, users.size());
        adminMap.put(group, admin);
        groupUserMap.put(group, users);
        groupMessageMap.put(group, new ArrayList<Message>());
        return group;
    }

    public int createMessage(String content) {
        messageId += 1;
        Message message = new Message(messageId, content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(groupUserMap.containsKey(group)) {
            boolean userFound = false;
            for(User user : groupUserMap.get(group)) {
                if(user.equals(sender)) {
                    userFound = true;
                    break;
                }
            }
            if(userFound) {
                senderMap.put(message, sender);
                List<Message> messages = groupMessageMap.get(group);
                messages.add(message);
                groupMessageMap.put(group, messages);
                return messages.size();
            }
            throw new Exception("Sender is not a member of the group");
        }
        throw new Exception("Group does not Exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(groupUserMap.containsKey(group)) {
            if(adminMap.get(group).equals(approver)){
                boolean userFound = false;
                for(User participants : groupUserMap.get(group)) {
                    if(user.equals(participants)) {
                        userFound = true;
                        break;
                    }
                }
                if(userFound) {
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a part of the group");
            }
            throw new Exception("Approver is not the current admin of the group");
        }
        throw new Exception("Group does not Exist");
    }

//    If the user is not found in any group, the application will throw an exception.
//    If the user is found in a group and is the admin, the application will throw an exception.
//    If the user is not the admin, the application will remove the user from the group,
//    remove all its messages from all the databases, and update relevant attributes accordingly.
//    If the user is removed successfully, the application will
//    return (the updated number of users in the group + the updated number of messages in the group
//     + the updated number of overall messages across all groups).
    public int removeUser(User user) throws Exception {
        Boolean userFound = false;
        Group userGroup = null;
        for (Group group : groupUserMap.keySet()) {
            List<User> participants = groupUserMap.get(group);
            for (User participant : participants) {
                if (participant.equals(user)) {
                    if (adminMap.get(group).equals(user)) {
                        throw new Exception("Admin cannot be removed");
                    }
                    userGroup = group;
                    userFound = true;
                    break;
                }
            }
            if (userFound) {
                break;
            }
        }
        if(userFound) {

        }
        throw new Exception("User not found in any group");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> list = new ArrayList<>();
        for(Message m : senderMap.keySet()){
            if(m.getTimestamp().compareTo(start)>0 && m.getTimestamp().compareTo(end)<0){
                list.add(m);
            }
        }
        Collections.sort(list,(a,b)->a.getTimestamp().compareTo(b.getTimestamp()));
        if(k>list.size()){
            throw new Exception("K is greater than the number of messages");
        }
        return list.get(k-1).getContent();
    }
}
