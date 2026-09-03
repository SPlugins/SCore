package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.ComparatorFeature;
import com.ssomar.score.features.types.PlaceholderConditionTypeFeature;
import com.ssomar.score.utils.emums.Comparator;
import com.ssomar.score.utils.emums.PlaceholdersCdtType;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class If extends PlayerCommand {

    public If() {
        setCanExecuteCommands(true);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        List<String> args = sCommandToExec.getOtherArgs();
    
        String condition = args.get(0);
        SsomarDev.testMsg("IF condition: " + condition, true);
    
        StringPlaceholder sp = aInfo.getSp();
        if (sp == null) sp = new StringPlaceholder();
        sp.setPlayerPlcHldr(receiver.getUniqueId(), aInfo.getSlot());
        sp.reloadAllPlaceholders();
    
        List<Player> targets = new ArrayList<>();
        targets.add(receiver);
    
        boolean finalResult = evaluateCondition(condition, receiver, sp);
        
        if (finalResult) {
            CommmandThatRunsCommand.runPlayerCommands(targets, args.subList(1, args.size()), aInfo);
        } else {
            SsomarDev.testMsg("IF STOPPED for condition > "+condition, true);
        }
    }

    public static boolean evaluateCondition(String condition, Player receiver, StringPlaceholder sp) {
        // Remove any whitespace for easier processing
        condition = condition.replaceAll("\\s+", "");
        condition = StringConverter.deconvertColor(condition);
    
        // Use two stacks to manage conditions and operators
        Stack<Boolean> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
    
        for (int i = 0; i < condition.length(); i++) {
            char ch = condition.charAt(i);
    
            // If current char is '(', push it to operators stack
            if (ch == '(') {
                operators.push(ch);
            }
            // If current char is ')', solve the entire expression till '('
            else if (ch == ')') {
                while (operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop(); // Pop '('
            }
            // If current char is a logical operator (&& or ||)
            else if (ch == '&' || ch == '|') {
                // Detect double symbols (&& or ||)
                if (i + 1 < condition.length() && condition.charAt(i + 1) == ch) {
                    char operator = (ch == '&') ? '&' : '|';
                    while (!operators.isEmpty() && hasPrecedence(operator, operators.peek())) {
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                    }
                    operators.push(operator);
                    i++; // Skip next character (& or | again)
                }
            }
            // If current char is part of a condition, extract and evaluate it
            else {
                StringBuilder cond = new StringBuilder();
                while (i < condition.length() && condition.charAt(i) != '(' && condition.charAt(i) != ')' && condition.charAt(i) != '&') {
                    // SCore's %rand% placeholder uses '|' to separate number ranges, so extra logic is required to properly detect "||" and "&&" char sequences
                    char c = condition.charAt(i);
                    char next = (i + 1 < condition.length()) ? condition.charAt(i + 1) : '\0';

                    if ((c == '|' && next == '|') || (c == '&' && next == '&')) {
                        break;
                    }
                    cond.append(condition.charAt(i));
                    i++;
                }
                i--; // Adjust index for next iteration
                boolean result = evaluateSingleCondition(cond.toString(), receiver, sp);
                values.push(result);
            }
        }
    
        // Entire expression has been parsed, apply remaining operators
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }
    
        return values.pop();
    }
    
    // Apply a logical operator to two boolean values
    private static boolean applyOperator(char operator, boolean b1, boolean b2) {
        if (operator == '&') return b1 && b2;
        if (operator == '|') return b1 || b2;
        throw new IllegalArgumentException("Invalid operator: " + operator);
    }
    
    // Return true if 'op2' has higher or same precedence as 'op1', false otherwise
    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        return (op1 != '&' || op2 != '|'); // AND has higher precedence than OR
    }
    private static boolean evaluateSingleCondition(String condition, Player receiver, StringPlaceholder sp) {
        PlaceholderConditionFeature conditionFeature = PlaceholderConditionFeature.buildNull();
        conditionFeature.setType(PlaceholderConditionTypeFeature.buildNull(PlaceholdersCdtType.PLAYER_PLAYER));

        //SsomarDev.testMsg("IF condition to evaluate: " + condition.replaceAll("§", "&"), true);
    
        boolean conditionContainsPlaceholder = condition.contains("%");
        String split = conditionContainsPlaceholder ? "%" : "";
    
        Comparator comparator = null;
        for (Comparator c : Comparator.values()) {
            if (condition.contains(split + c.getSymbol())) {
                conditionFeature.setComparator(ComparatorFeature.buildNull(c));
                comparator = c;
                break;
            }
        }
        if (comparator == null) {
            SsomarDev.testMsg("IF STOPPED because comparator null for condition: " + condition, true);
            return false;
        }
    
        String[] parts = condition.split(split + comparator.getSymbol());
        if (parts.length < 2) {
            SsomarDev.testMsg("IF STOPPED because parts are invalid for condition: " + condition, true);
            return false;
        }

        String part1 = parts[0].trim()+ split;
        String part2 = parts[1].trim();
        //SsomarDev.testMsg("IF part1: " + part1 + ", part2: " + part2, true);
    
        conditionFeature.setPart1(ColoredStringFeature.buildNull(part1));
        conditionFeature.setPart2(ColoredStringFeature.buildNull(part2));
    
        return conditionFeature.verify(receiver, null, sp);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {

        if (args.size() < 2) return Optional.of(notEnoughArgs + getTemplate());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("IF");
        return names;
    }

    @Override
    public String getTemplate() {
        return "IF {condition_without_spaces} {command1} <+> {command2} <+> ...";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

}
