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
    String hashprev;
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

        for (String line : strBlock.split("\n")) {
            if (line.toLowerCase().startsWith("id:")) {
                String id = line.split(":")[1].strip().replace("-", "");
                boolean isNumeric = id.chars().allMatch(Character::isDigit);

                if (!isNumeric) {
                    throw new BlockParseException("Id should be a number");
                }
                block.id = Integer.parseInt(id);
                break;
            }
        }

        List<String> lines = strBlock
                .lines()
                .map(String::strip)
                .filter(e -> e.length() > 0)
                .collect(Collectors.toList());

        if (lines.size() != 7) {
            throw new BlockParseException("Every Block should " +
                    "contain 7 lines of data");
        }

        if (!lines.get(0).toLowerCase().startsWith("block") && !lines.get(0).toLowerCase().startsWith("genesis block")) {
            throw new BlockParseException("The first line of the first block in the blockchain should be \"Genesis Block:\" and every subsequent Block's first line should be \"Block:\"" +
                    "\nYour program instead printed as the first line in Block " + block.id + ": " + "\"" + lines.get(0) + "\"");
        }

        if (!lines.get(1).toLowerCase().startsWith("id:")) {
            throw new BlockParseException("Second line of every Block " +
                    "should start with \"Id:\"");
        }

        String id = lines.get(1).split(":")[1].strip();
        boolean isNumeric = id.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Id should be a number");
        }

        block.id = Integer.parseInt(id);

        if (!lines.get(2).toLowerCase().startsWith("timestamp:")) {
            throw new BlockParseException("Third line of every Block " +
                    "should start with \"Timestamp:\"");
        }

        String timestamp = lines.get(2).split(":")[1].strip();
        isNumeric = timestamp.chars().allMatch(Character::isDigit);

        if (!isNumeric) {
            throw new BlockParseException("Timestamp should be a number");
        }

        block.timestamp = Long.parseLong(timestamp);

        if (!lines.get(3).equalsIgnoreCase("hash of the previous block:")) {
            throw new BlockParseException("4-th line of every Block " +
                    "should be \"Hash of the previous block:\"");
        }

        if (!lines.get(5).equalsIgnoreCase("hash of the block:")) {
            throw new BlockParseException("6-th line of every Block " +
                    "should be \"Hash of the block:\"");
        }

        String prevhash = lines.get(4).strip();
        String hash = lines.get(6).strip();

        if (!(prevhash.length() == 64 || prevhash.equals("0"))
                || !(hash.length() == 64)) {

            throw new BlockParseException("Hash length should " +
                    "be equal to 64 except \"0\"");
        }

        if (hash.equals(prevhash)) {
            throw new BlockParseException("The current hash and the previous hash in a block should be different.");
        }

        block.hash = hash;
        block.hashprev = prevhash;

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
        String[] strBlocks = output.split("\n\n");

        List<Block> blocks = new ArrayList<>();

        for (String strBlock : strBlocks) {
            Block block = parseBlock(strBlock.strip());
            if (block != null) {
                blocks.add(block);
            }
        }

        return blocks;
    }
}


public class BlockchainTest extends StageTest {

    List<String> previousOutputs = new ArrayList<>();


    @Override
    public List<TestCase> generate() {
        return List.of(
                new TestCase(),
                new TestCase()
        );
    }

    @Override
    public CheckResult check(String reply, Object clue) {

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

        Block first = blocks.get(0);
        if (!first.hashprev.equals("0")) {
            return new CheckResult(false,
                    "Previous hash of the first block should be \"0\"");
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

            if (!next.hashprev.equals(curr.hash)) {
                return new CheckResult(false, "Two hashes aren't equal, " +
                        "but should");
            }
        }


        return CheckResult.correct();
    }
}
