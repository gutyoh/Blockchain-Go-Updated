import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.TestCase;

import java.util.ArrayList;
import java.util.List;
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

    static Block parseBlock(String strBlock) throws BlockParseException {
        if (strBlock.length() == 0) {
            return null;
        }

        if (!(strBlock.toLowerCase().contains("block")
                && strBlock.toLowerCase().contains("timestamp"))) {

            return null;
        }

        Block block = new Block();

        List<String> lines = strBlock
                .lines()
                .map(String::strip)
                .filter(e -> e.length() > 0)
                .collect(Collectors.toList());

        if (lines.size() != 9) {
            throw new BlockParseException("Every Block should " +
                    "contain 9 lines of data");
        }

        if (!lines.get(0).toLowerCase().startsWith("block") && !lines.get(0).toLowerCase().startsWith("genesis block")) {
            throw new BlockParseException("The first line of the first block in the blockchain should be \"Genesis Block:\" and every subsequent Block's first line should be \"Block:\"" +
                    "\nYour program instead printed as the first line in Block " + block.id + ": " + "\"" + lines.get(0) + "\"");
        }

        if (!lines.get(1).toLowerCase().startsWith("id:")) {
            throw new BlockParseException("Second line of every Block " +
                    "should start with \"Id:\"");
        }

        String id = lines.get(1).split(":")[1]
                .strip().replace("-", "");
        boolean isNumeric = id.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Id should be a number");
        }

        block.id = Integer.parseInt(id);

        if (!lines.get(2).toLowerCase().startsWith("timestamp:")) {
            throw new BlockParseException("Third line of every Block " +
                    "should start with \"Timestamp:\"");
        }

        String timestamp = lines.get(2).split(":")[1]
                .strip().replace("-", "");
        isNumeric = timestamp.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Timestamp should be a number");
        }

        block.timestamp = Long.parseLong(timestamp);

        if (!lines.get(3).toLowerCase().startsWith("magic number:")) {
            throw new BlockParseException("4-th line of every Block " +
                    "should start with \"Magic number:\"");
        }

        String magic = lines.get(3).split(":")[1]
                .strip().replace("-", "");
        isNumeric = magic.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Timestamp should be a number");
        }

        block.magic = Long.parseLong(magic);

        if (!lines.get(4).equalsIgnoreCase("hash of the previous block:")) {
            throw new BlockParseException("5-th line of every Block " +
                    "should be \"Hash of the previous block:\"");
        }

        if (!lines.get(6).equalsIgnoreCase("hash of the block:")) {
            throw new BlockParseException("7-th line of every Block " +
                    "should be \"Hash of the block:\"");
        }

        String prevHash = lines.get(5).strip();
        String hash = lines.get(7).strip();

        if (!(prevHash.length() == 64 || prevHash.equals("0"))
                || !(hash.length() == 64)) {

            throw new BlockParseException("Hash length should " +
                    "be equal to 64 except \"0\"");
        }

        if (hash.equals(prevHash)) {
            throw new BlockParseException("The current hash and the previous hash in a block should be different.");
        }

        block.hash = hash;
        block.prevHash = prevHash;

        // Check the First/Genesis block:
        if (block.id == 1) {
            if (!lines.get(0).toLowerCase().contains("genesis block")) {
                throw new BlockParseException(
                        "First line of the First/Genesis Block should be \"Genesis Block:\"");
            }
        }

        // Check the other blocks:
        if (1 < block.id && block.id < 5) {
            if (!lines.get(0).toLowerCase().startsWith("block")) {
                throw new BlockParseException(
                        "First line of every other Block should start with \"Block\"");
            }
        }

        return block;
    }


    static List<Block> parseBlocks(String output) throws BlockParseException {
        // String[] strBlocks = output.substring(
            // output.indexOf("Block:")).split("\n\n");

        String[] strBlocks = output.split("\n\n");

        List<Block> blocks = new ArrayList<>();

        for (String strBlock : strBlocks) {
            if (strBlock.toLowerCase().startsWith("enter how many zeros the hash must start with")) {
                continue;
            }

            Block block = parseBlock(strBlock.strip());
            if (block != null) {
                blocks.add(block);
            }
        }

        return blocks;
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

    @Override
    public List<TestCase<Clue>> generate() {
        return List.of(
                new TestCase<Clue>().setInput("0").setAttach(new Clue(0)),
                new TestCase<Clue>().setInput("1").setAttach(new Clue(1)),
                new TestCase<Clue>().setInput("2").setAttach(new Clue(2)),
                new TestCase<Clue>().setInput("0").setAttach(new Clue(0)),
                new TestCase<Clue>().setInput("1").setAttach(new Clue(1)),
                new TestCase<Clue>().setInput("2").setAttach(new Clue(2))
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
            return CheckResult.wrong("");
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
                        "Id's of blocks should increase by 1");
            }

            if (next.timestamp < curr.timestamp) {
                return new CheckResult(false,
                        "Timestamp's of blocks should increase");
            }

            if (!next.prevHash.equals(curr.hash)) {
                return new CheckResult(false, "Two hashes aren't equal, " +
                        "but should");
            }

            if (!next.hash.startsWith(clue.zeros)) {
                return new CheckResult(false,
                        "Hash should start with some zeros");
            }
        }

        return CheckResult.correct();
    }
}
