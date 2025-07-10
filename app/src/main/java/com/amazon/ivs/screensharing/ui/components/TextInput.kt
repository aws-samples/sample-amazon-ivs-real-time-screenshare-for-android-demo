package com.amazon.ivs.screensharing.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amazon.ivs.screensharing.ui.theme.TextStylePrimary

@Composable
private fun textFieldColors(
    backgroundColor: Color = Color.Transparent,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    hintColor: Color = MaterialTheme.colorScheme.onPrimary,
) = TextFieldDefaults.colors(
    cursorColor = textColor,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedLabelColor = textColor,
    unfocusedLabelColor = textColor,
    unfocusedPlaceholderColor = hintColor,
    focusedPlaceholderColor = hintColor,
    disabledPlaceholderColor = hintColor,
    focusedContainerColor = backgroundColor,
    unfocusedContainerColor = backgroundColor,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor =  Color.Transparent,
    selectionColors = TextSelectionColors(
        handleColor = textColor,
        backgroundColor = textColor.copy(alpha = 0.2f)
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInput(
    hint: String = "",
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    text: TextFieldValue = TextFieldValue(),
    maxLines: Int = 1,
    singleLine: Boolean = true,
    fillMaxWidth: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    hintColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
    colors: TextFieldColors = textFieldColors(
        backgroundColor = backgroundColor,
        textColor = textColor,
        hintColor = hintColor
    ),
    textStyle: TextStyle = TextStylePrimary.copy(color = textColor),
    hintStyle: TextStyle = TextStylePrimary.copy(color = hintColor),
    shape: Shape = RoundedCornerShape(8.dp),
    borderWidth: Dp = 1.dp,
    imeAction: ImeAction = ImeAction.Done,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    onValueChanged: (TextFieldValue) -> Unit = {},
    onImeAction: (String) -> Unit = {}
) {
    val label: (@Composable () -> Unit)? = if (text.text.isBlank()) {{
        Text(
            text = hint,
            style = hintStyle,
        )
    }} else null
    CompositionLocalProvider(
        LocalTextSelectionColors provides colors.textSelectionColors
    ) {
        BasicTextField(
            modifier = modifier
                .thenOptional(enabled = fillMaxWidth) {
                    fillMaxWidth()
                }
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = shape
                ),
            value = text,
            maxLines = maxLines,
            singleLine = singleLine,
            textStyle = textStyle,
            onValueChange = onValueChanged,
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization,
                imeAction = imeAction,
                keyboardType = keyboardType
            ),
            keyboardActions = KeyboardActions(
                onAny = {
                    onImeAction(text.text)
                },
            ),
            cursorBrush = SolidColor(textColor),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = text.text,
                    innerTextField = innerTextField,
                    placeholder = label,
                    shape = shape,
                    singleLine = singleLine,
                    enabled = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    colors = colors,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputPreviewDark() {
    TextInputPreview()
}

@Preview
@Composable
private fun TextInputPreviewLight() {
    TextInputPreview()
}

@Composable
private fun TextInputPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextInput(
                text = TextFieldValue("Text")
            )
            TextInput(
                hint = "Hint"
            )
        }
    }
}
