package com.evacoffee.beautymod.dialogue;

import java.util.HashMap;
import java.util.Map;

public class CharacterDialogues {
    
    public static class CharacterDialogue {
        public final String[] greetings;
        public final String[] conversationStarters;
        public final String[] flirtyLines;
        public final String[] personalQuestions;
        public final String[] dateInvitations;

        public CharacterDialogue(String[] greetings, String[] conversationStarters, 
                               String[] flirtyLines, String[] personalQuestions, 
                               String[] dateInvitations) {
            this.greetings = greetings;
            this.conversationStarters = conversationStarters;
            this.flirtyLines = flirtyLines;
            this.personalQuestions = personalQuestions;
            this.dateInvitations = dateInvitations;
        }
    }

    public static final Map<String, CharacterDialogue> CHARACTER_DIALOGUES = new HashMap<>();

    static {
        // Luna (Shy)
        CHARACTER_DIALOGUES.put("luna", new CharacterDialogue(
            new String[]{
                "O-oh! H-hi there... I didn't see you coming.",
                "Um... hello again. It's nice to see you.",
                "You're... you're back. That's... good."
            },
            new String[]{
                "The flowers here are... really pretty today, don't you think?",
                "I was just reading this book... um, would you like to join me?",
                "Sometimes I come here to be alone... but I don't mind if it's with you."
            },
            new String[]{
                "Y-your smile... it's really nice...",
                "I wrote a poem about you... but I'm too embarrassed to show you.",
                "When you're around, my heart feels like it's going to burst..."
            },
            new String[]{
                "What's your favorite place to be alone?",
                "Do you ever feel like no one really understands you?",
                "What makes you feel brave?"
            },
            new String[]{
                "There's a quiet spot by the river... w-would you like to go there with me?",
                "I found some wildflowers growing... maybe we could pick some together?",
                "The stars are beautiful tonight... if you're not too busy..."
            }
        ));

        // Zara (Flirty)
        CHARACTER_DIALOGUES.put("zara", new CharacterDialogue(
            new String[]{
                "Well, well, look who decided to grace me with their presence~",
                "If it isn't my favorite person in the whole world!",
                "Hey there, gorgeous. Miss me?"
            },
            new String[]{
                "I was just thinking about how much more fun this place would be if we caused some trouble together.",
                "You know, I've been watching you... and I like what I see.",
                "Tell me something exciting about yourself. I'm all ears~"
            },
            new String[]{
                "Is it hot in here, or is it just you? Oh wait, that's definitely you.",
                "I'd flirt with you, but I'd rather seduce you with my awkwardness.",
                "Are you a magician? Because whenever I look at you, everyone else disappears."
            },
            new String[]{
                "What's the most rebellious thing you've ever done?",
                "Do you believe in love at first sight, or should I walk by again?",
                "What's your idea of a perfect date? And don't say April 25th."
            },
            new String[]{
                "I know this great little place where we won't get caught... interested?",
                "Let's go do something that would make your parents disapprove.",
                "I dare you to come on an adventure with me. What do you say?"
            }
        ));

        // Mira (Artistic)
        CHARACTER_DIALOGUES.put("mira", new CharacterDialogue(
            new String[]{
                "Ah, perfect timing! The light is just right for inspiration.",
                "Hello, my muse. What brings you to my corner of the world?",
                "I was just thinking about how to capture your essence in my next piece."
            },
            new String[]{
                "Do you see how the light plays on the water? It's magical, isn't it?",
                "I've been working on something new... would you like to see?",
                "They say art is subjective. What does it make you feel?"
            },
            new String[]{
                "You're like a masterpiece I could stare at forever.",
                "If you were a color, you'd be the rarest shade of perfection.",
                "I've painted a thousand sunsets, but none compare to your smile."
            },
            new String[]{
                "What's the most beautiful thing you've ever seen?",
                "If you could create anything in the world, what would it be?",
                "What does happiness look like to you?"
            },
            new String[]{
                "I found the perfect spot to watch the sunset. Join me?",
                "I'm setting up my easel by the lake. Keep me company?",
                "There's an art exhibit in town. Would you be my date?"
            }
        ));

        // Celia (Cool)
        CHARACTER_DIALOGUES.put("celia", new CharacterDialogue(
            new String[]{
                "Hey. You're right on time.",
                "Was wondering when I'd see you.",
                "You look... good."
            },
            new String[]{
                "Nice weather for a walk, don't you think?",
                "I was just about to head to the market. Need anything?",
                "You seem like you've got something on your mind."
            },
            new String[]{
                "I don't usually do this, but... you're different.",
                "You've got this way of making everything else fade away.",
                "I don't say this often, but... I like having you around."
            },
            new String[]{
                "What's something you're passionate about?",
                "Who's the most important person in your life?",
                "What's one thing you'd change about your past?"
            },
            new String[]{
                "I'm going stargazing tonight. You in?",
                "There's a quiet spot I know. Less people. Wanna check it out?",
                "I've got two tickets to the concert. Come with me?"
            }
        ));

        // Kai (Charming)
        CHARACTER_DIALOGUES.put("kai", new CharacterDialogue(
            new String[]{
                "Ah, the most beautiful person in the room has arrived.",
                "Do you believe in fate? Because I was just thinking about you.",
                "Every time I see you, the world stops for a moment."
            },
            new String[]{
                "Tell me, what's your secret to being so captivating?",
                "I was just about to write a sonnet. Care to be my muse?",
                "You have the most interesting aura about you. What's your story?"
            },
            new String[]{
                "If I had a star for every time you brightened my day, I'd have a galaxy in my hands.",
                "Are you a magician? Because whenever I look at you, everyone else disappears.",
                "Do you have a name, or can I call you mine?"
            },
            new String[]{
                "What's your idea of a perfect day?",
                "What's the most romantic thing you've ever done?",
                "If you could have dinner with anyone, living or dead, who would it be?"
            },
            new String[]{
                "I've reserved a table under the stars. Join me for dinner?",
                "There's a moonlit garden I'd love to show you. Tonight?",
                "How about we dance until the sun comes up?"
            }
        ));

        // Dante (Mysterious)
        CHARACTER_DIALOGUES.put("dante", new CharacterDialogue(
            new String[]{
                "You're more observant than most... I like that.",
                "I had a feeling I'd see you today.",
                "The cards told me someone interesting would appear..."
            },
            new String[]{
                "Do you believe in fate, or do we make our own destiny?",
                "There's something different about you... I can't quite place it.",
                "The night holds many secrets. Care to discover some with me?"
            },
            new String[]{
                "I've met many people, but you... you're a riddle I'd like to solve.",
                "If I told you I could read minds, would you believe me?",
                "There's a spark between us... can you feel it too?"
            },
            new String[]{
                "What's the most mysterious thing that's ever happened to you?",
                "Do you believe in things that can't be explained?",
                "What's your greatest secret?"
            },
            new String[]{
                "I know a place where the veil between worlds is thin. Care to see?",
                "There's a hidden garden that only blooms at midnight. Join me?",
                "How about an adventure into the unknown? Just you and me."
            }
        ));

        // Theo (Sweet)
        CHARACTER_DIALOGUES.put("theo", new CharacterDialogue(
            new String[]{
                "Oh! I made your favorite cookies! Would you like one?",
                "I was just thinking about you! How are you today?",
                "You came! I was hoping you'd stop by."
            },
            new String[]{
                "I planted some new flowers today. Would you like to see?",
                "I found this recipe I think you'd love. Want to try making it together?",
                "You always know how to make me smile. How do you do that?"
            },
            new String[]{
                "If you were a cookie, you'd be a sweet one.",
                "I think you're the missing ingredient in my recipe for happiness.",
                "Do you believe in soulmates? Because I think we might be."
            },
            new String[]{
                "What's your favorite childhood memory?",
                "What's the nicest thing anyone's ever done for you?",
                "What makes you feel most loved?"
            },
            new String[]{
                "I packed a picnic! Would you like to join me in the meadow?",
                "I'm baking cookies this afternoon. Want to help?",
                "There's a cozy little café I've been wanting to try. Will you come with me?"
            }
        ));

        // Rico (Bold)
        CHARACTER_DIALOGUES.put("rico", new CharacterDialogue(
            new String[]{
                "Hey there, partner! Ready for an adventure?",
                "There you are! I was just about to go looking for you!",
                "Boom! The party can start now that you're here!"
            },
            new String[]{
                "I've got this crazy idea... you in?",
                "What's the most exciting thing you've ever done?",
                "Let's make today unforgettable, what do you say?"
            },
            new String[]{
                "Are you a parking ticket? Because you've got FINE written all over you!",
                "If you were a vegetable, you'd be a cutecumber!",
                "Do you have a map? I keep getting lost in your eyes!"
            },
            new String[]{
                "What's on your bucket list? Let's do it!",
                "What's the wildest dream you've ever had?",
                "If you could do anything without fear of failing, what would it be?"
            },
            new String[]{
                "I'm going skydiving tomorrow. Wanna come?",
                "Let's go on a road trip and see where the wind takes us!",
                "There's this amazing view from the top of the mountain. Race you there?"
            }
        ));
    }

    public static CharacterDialogue getDialogueForCharacter(String characterId) {
        return CHARACTER_DIALOGUES.getOrDefault(characterId.toLowerCase(), 
            new CharacterDialogue(
                new String[]{"Hello there!"}, 
                new String[]{"Nice weather we're having."}, 
                new String[]{"You're looking lovely today."}, 
                new String[]{"What's on your mind?"}, 
                new String[]{"Would you like to go somewhere?"}
            )
        );
    }
}
