import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


class BlockParseException extends Exception {
    BlockParseException(String msg) {
        super(msg);
    }
}


class Block {

    int id;
    long timestamp;
    long magic;
    String prevHash;
    String hash;

    static ArrayList<String> minerIds;
    static int N;

    String[] blockDataMessage = {
            // Test case #1
            // Block 2
            "Tom: Hey, I'm first",

            // Block 3
            "Alice: It's not fair! You always will be first because it is your blockchain!",

            // Block 4
            "Alice: Anyway, thank you for this amazing chat",

            // Block 5
            "Tom: You're welcome, Alice :)",

            // ----------------------------------------------

            // Test case #2
            // Block 2
            "Tom: Hey, I'm first once again!",

            // Block 3
            "Nick: Hey Tom, nice Blockchain chat you created!",

            // Block 4
            "Tom: Thanks, Nick! It was a lot of fun to create it!",

            // Block 5
            "Tom: Anyways, I have to leave for a meeting now. Enjoy the blockchain chat. Bye!",
    };


    static Block parseBlock(String strBlock) throws BlockParseException {
        if (strBlock.length() == 0) {
            return null;
        }

        if (!(strBlock.contains("Block:")
                && strBlock.contains("Timestamp:"))) {

            return null;
        }

        Block block = new Block();

        List<String> lines = strBlock
                .lines()
                .map(String::strip)
                .filter(e -> e.length() > 0)
                .collect(Collectors.toList());

        if (lines.size() < 12) {
            if (block.id > 5) {
                throw new BlockParseException("The program should ONLY create and print 5 blocks!");
            } else {
                throw new BlockParseException("Every block should " +
                        "contain at least 12 lines of data");
            }
        }

//        if (!lines.get(0).equals("Block:")) {
//            throw new BlockParseException("First line of every block " +
//                    "should be \"Block:\"");
//        }

        if (!lines.get(1).toLowerCase().startsWith("created by")) {
            throw new BlockParseException("Second line of every block should start with \"Created by\"");
        }

        minerIds.add(lines.get(1));

        if (!lines.get(2).toLowerCase().startsWith("id:")) {
            throw new BlockParseException("Third line of every block should start with \"Id:\"");
        }

        String id = lines.get(2).split(":")[1]
                .strip().replace("-", "");
        boolean isNumeric = id.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Id should be a number");
        }

        block.id = Integer.parseInt(id);

        if (!lines.get(3).toLowerCase().startsWith("timestamp:")) {
            throw new BlockParseException("4-th line of every block should start with \"Timestamp:\"");
        }

        String timestamp = lines.get(3).split(":")[1]
                .strip().replace("-", "");
        isNumeric = timestamp.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Timestamp should be a number");
        }

        block.timestamp = Long.parseLong(timestamp);


        if (!lines.get(4).toLowerCase().startsWith("magic number:")) {
            throw new BlockParseException("5-th line of every block should start with \"Magic number:\"");
        }

        String magic = lines.get(4).split(":")[1]
                .strip().replace("-", "");
        isNumeric = magic.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Magic number should be a number");
        }

        block.magic = Long.parseLong(magic);


        if (!lines.get(5).equalsIgnoreCase("hash of the previous block:")) {
            throw new BlockParseException("6-th line of every block should start with \"Hash of the previous block:\"");
        }

        if (!lines.get(7).equalsIgnoreCase("hash of the block:")) {
            throw new BlockParseException("8-th line of every block should start with \"Hash of the block:\"");
        }

        String prevHash = lines.get(6).strip();
        String hash = lines.get(8).strip();

        if (!(prevHash.length() == 64 || prevHash.equals("0"))
                || !(hash.length() == 64)) {

            throw new BlockParseException("Hash length should " +
                    "be equal to 64 except \"0\"");
        }

        if (hash.equals(prevHash)) {
            throw new BlockParseException("The current hash and the previous hash in a block should be different.");
        }

        if (!hash.startsWith("0".repeat(N))) {
            throw new BlockParseException(String.format("N is %d but hash, %s , did not start with the correct number of zeros.", N, hash));
        }

        block.hash = hash;
        block.prevHash = prevHash;

        // Check the `Block data` of the first/genesis block:
        if (block.id == 1) {
            if (!lines.get(0).toLowerCase().contains("genesis block")) {
                throw new BlockParseException(
                        "First line of the First/Genesis Block should be \"Genesis Block:\"");
            }

            if (!lines.get(9).startsWith("Block data:")) {
                throw new BlockParseException("10-th line of the first block " +
                        "should start with \"Block data:\"");
            }

            if (!lines.get(10).toLowerCase().contains("no messages")) {
                throw new BlockParseException("11-th line of the first block " +
                        "should contain \"no messages\"");
            }

            if (!(lines.get(11).toLowerCase().contains("block") || lines.get(11).toLowerCase().contains("generating"))) {
                throw new BlockParseException("12-th line of the first block " +
                        "should say how long the block was generating for! " +
                        "(Use the example's format)" +

                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(11));
            }

            if (!lines.get(12).toUpperCase().startsWith("N ")) {
                throw new BlockParseException("13-th line of the first block " +
                        "should be state what happened to N in the format given." +

                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(11));
            }

            if (12 != lines.size() - 1) {
                throw new BlockParseException("Your program printed in Block" + block.id +
                        " after the line: \"N was increased/decreased/stays the same\"\n" +
                        "an additional and unexpected line: " + "\"" + lines.get(12) + "\"");
            }
        }

        // Then check the `Block data` of the other blocks:
        if (1 < block.id && block.id < 5) {
            if (!lines.get(0).toLowerCase().startsWith("block")) {
                throw new BlockParseException(
                        "First line of every other Block should start with \"Block\"");
            }

            if (!lines.get(9).toLowerCase().startsWith("block data:")) {
                throw new BlockParseException("10-th line of every block should start with \"Block data:\"" +
                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(9));
            }

            int i = 10;

            if (!Arrays.asList(block.blockDataMessage).contains(lines.get(i))) {
                throw new BlockParseException("In Block" + block.id + " the chat message within the Block data "
                        + "should be: " + block.blockDataMessage[0] +

                        "\n" + "Your program instead printed in Block" + block.id +
                        " an unexpected line: " + lines.get(i));
            }

            while(!lines.get(i).toLowerCase().startsWith("message")) {
                i++;
            }

            if(!lines.get(i).toLowerCase().startsWith("message")) {
                throw new BlockParseException("After the line with \"Block data:\" " +
                        "the next line should contain the message " +
                        "ID and start with \"Message ID:\" " +
                        "followed by a unique ID for the message(s)." +
                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            // Get the Message ID after the `:` colon in the string `Message ID:`
            String messageID = lines.get(i).split(":")[1].strip();

            // Check if the Message ID is a double SHA256 hash
            if (!(messageID.length() == 64 && messageID.matches("[a-zA-Z0-9]+"))) {
                throw new BlockParseException("The Message ID should be a double SHA256 hash created using the entire message string" +
                        "\n" + "Your program instead printed in Block" +
                        block.id + " an incorrect Message ID: " + messageID);
            }

            i++; // Get the line — `Signature: <Signature>`

            if(!lines.get(i).toLowerCase().startsWith("signature")) {
                throw new BlockParseException("After the line with \"Message ID:\" " +
                        "the next line should contain the message signature " +
                        "and start with \"Signature:\" " +
                        "followed by the message signature(s)." +
                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            // Get the signature value after the `:` colon in the string `Signature:`
            String signature = lines.get(i).split(":")[1].strip();

            if (signature == null) {
                throw new BlockParseException("Make sure you write the signature after the `Signature:` string.\n" +
                        "For example: \"Signature:MEUCIBFU...\"" +

                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            // Check if the signature starts with `ME` and has a length of 96 characters:
            if (!signature.startsWith("ME") || signature.length() != 96) {
                throw new BlockParseException("The Signature should be ASN.1 encoded and " +
                        "have a length of 96 characters.\n" +
                        "Your Signature: " + signature + "\n" +
                        "Your Signature length: " + signature.length());
            }

            i++;  // Get the line — `Public Key:`

            if (!lines.get(i).toLowerCase().startsWith("public key")) {
                throw new BlockParseException("After the line with \"Signature:\" " +
                        "the next line should contain the message public key " +
                        "and start with \"Public Key:\"" +

                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            // Get the Public Key value after the `:` colon in the string `Public Key:`
            String publicKey = lines.get(i).split(":")[1].trim();
            if (publicKey.isEmpty()) {
                throw new BlockParseException("Make sure you write the public key after the \"Public Key:\" string.\n" +
                        "For example \"Public Key: MFkw...\"");
            }

            // check if the public key starts with MF and has a length of 120 or 124 characters
            if (!publicKey.startsWith("MF") || (publicKey.length() != 120 && publicKey.length() != 124)) {
                throw new BlockParseException("The Public Key should be in the PKIX, ASN.1 DER form and have a " +
                        "length of 120 or 124 characters.\n" +
                        "Your Public Key: " + publicKey + "\n" +
                        "Your Public Key length: " + publicKey.length());
            }

            i++;  // Get the line — `Block was generating for ...`

            if (!lines.get(i).toLowerCase().contains("block") && !lines.get(i).toLowerCase().contains("generating")) {
                throw new BlockParseException("After the line with the Public Key of the message, " +
                        "the next line should state how long the block was generating for! " +
                        "(Use the example's format)" +

                        "\n" + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            i++;  // Get the line — `N ...`

            if (i != lines.size() - 1) {
                throw new BlockParseException("Your program printed in Block" + block.id +
                        " after the line: \"N was increased/decreased/stays the same\"\n" +
                        "an additional and unexpected line: " + "\"" + lines.get(lines.size() - 1) + "\"");
            }

            if (!lines.get(i).toUpperCase().startsWith("N ")) {
                throw new BlockParseException("After the line `Block was generating for ...` " +
                        "the next line should state what happened to N in the format given."
                        + "Your program instead printed in Block" +
                        block.id + " an unexpected line: " + lines.get(i));
            }

            if (lines.get(i).toLowerCase().contains("increase")) {
                N += 1;
            } else if (lines.get(i).toLowerCase().contains("decrease")) {
                N -= 1;
                if (N < 0) {
                    throw new BlockParseException("N was decreased below zero!");
                }
            } else if (!lines.get(i).toLowerCase().contains("same")) {
                throw new BlockParseException("The second to last line of every block EXCEPT for the fifth block" +
                        "must state N increased, decreased, or stayed the same.");
            }

        }

        return block;
    }


    static List<Block> parseBlocks(String output) throws BlockParseException {
        minerIds = new ArrayList<String>();
        N = 0;

        String[] strBlocks = output.split("\n\n");

        List<Block> blocks = new ArrayList<>();

        for (String strBlock : strBlocks) {
            if (strBlock.toLowerCase().startsWith("enter a single message")) {
                continue;
            }

            Block block = parseBlock(strBlock.strip());
            if (block != null) {
                blocks.add(block);
            }
        }

        String firstMiner = minerIds.get(0);
        minerIds.removeIf(s -> Objects.equals(s, firstMiner));
        if (minerIds.size() == 0) {
            throw new BlockParseException("All blocks are mined by a single miner!");
        }

        return blocks;
    }
}

class MessageParseException extends Exception {
    MessageParseException(String msg) {
        super(msg);
    }
}

class Message {

    public static void parseMessagePrompt(String output) throws MessageParseException {
        String[] strMessage = output.split("\n\n");

        // iterate over the odd lines in the strMessage list and check if they start with "enter a single message"
        for (int i = 1; i < strMessage.length; i += 2) {
            if (!strMessage[i].toLowerCase().startsWith("enter a single message")) {
                throw new MessageParseException(
                        "In every Block except for the fifth Block, after the line that states what happened to `N`\n"
                                + "\"N was increased/decreased/stays the same\" your program should prompt the user: "
                                + "\"Enter a single message to send to the Blockchain:\"\n"
                                + "Your program instead printed: " + "\"" + strMessage[i] + "\"");
            }
        }
    }
}


class Clue {
    String zeros;

    Clue(int n) {
        zeros = "0".repeat(n);
    }
}


public class BlockchainTest extends StageTest<Clue> {

    List<String> previousOutputs = new ArrayList<>();

    static String testInput1 = "Tom: Hey, I'm first\n" +
            "Alice: It's not fair! You always will be first because it is your blockchain!\n" +
            "Alice: Anyway, thank you for this amazing chat\n" +
            "Tom: You're welcome, Alice :)\n";

    static String testInput2 = "Tom: Hey, I'm first once again!\n" +
            "Nick: Hey Tom, nice Blockchain chat you created!\n" +
            "Tom: Thanks, Nick! It was a lot of fun to create it!\n" +
            "Tom: Anyways, I have to leave for a meeting now. Enjoy the blockchain chat. Bye!";

    @Override
    public List<TestCase<Clue>> generate() {
        return List.of(
                new TestCase<Clue>()
                        .setInput(testInput1)
                        .setInput(testInput2)
                        .setAttach(new Clue(0))
        );
    }

    @Override
    public CheckResult check(String reply, Clue clue) {

        if (previousOutputs.contains(reply)) {
            return new CheckResult(false,
                    "You already printed this text in the previous tests");
        }

        previousOutputs.add(reply);

        List<Block> blocks;
        try {
            blocks = Block.parseBlocks(reply);
        } catch (BlockParseException ex) {
            return new CheckResult(false, ex.getMessage());
        } catch (Exception ex) {
            return CheckResult.wrong("Something went wrong while parsing the block data:\n" + ex.getMessage());
        }

        // # NEW -- Parse the "enter single message" line
        try {
            Message.parseMessagePrompt(reply);
        } catch (MessageParseException ex) {
            return new CheckResult(false, ex.getMessage());
        } catch (Exception ex) {
            return CheckResult.wrong("Something went wrong while parsing the enter message prompt:\n" + ex.getMessage());
        }

        if (blocks.size() != 5) {
            return new CheckResult(false,
                    "You should output 5 blocks, found " + blocks.size());
        }

        for (int i = 1; i < blocks.size(); i++) {
            Block curr = blocks.get(i - 1);
            Block next = blocks.get(i);

            if (curr.id + 1 != next.id) {
                return new CheckResult(false,
                        "Id`s of blocks should increase by 1");
            }

            if (next.timestamp < curr.timestamp) {
                return new CheckResult(false,
                        "Timestamp`s of blocks should increase");
            }

            if (!next.prevHash.equals(curr.hash)) {
                return new CheckResult(false, "Two hashes aren't equal, " +
                        "but should");
            }
        }


        return CheckResult.correct();
    }
}