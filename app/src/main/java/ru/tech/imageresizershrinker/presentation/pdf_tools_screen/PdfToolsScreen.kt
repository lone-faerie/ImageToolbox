package ru.tech.imageresizershrinker.presentation.pdf_tools_screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.dynamic.theme.observeAsState
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.image_stitching_screen.components.ImageReorderCarousel
import ru.tech.imageresizershrinker.presentation.image_stitching_screen.components.ScaleSmallImagesToLargeToggle
import ru.tech.imageresizershrinker.presentation.pdf_tools_screen.viewModel.PdfToolsViewModel
import ru.tech.imageresizershrinker.presentation.root.utils.confetti.LocalConfettiController
import ru.tech.imageresizershrinker.presentation.root.utils.helper.Picker
import ru.tech.imageresizershrinker.presentation.root.utils.helper.localImagePickerMode
import ru.tech.imageresizershrinker.presentation.root.utils.helper.rememberImagePicker
import ru.tech.imageresizershrinker.presentation.root.utils.helper.showReview
import ru.tech.imageresizershrinker.presentation.root.utils.navigation.Screen
import ru.tech.imageresizershrinker.presentation.root.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.containerFabBorder
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.navBarsPaddingOnlyIfTheyAtTheEnd
import ru.tech.imageresizershrinker.presentation.root.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.presentation.root.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.presentation.root.widget.other.PdfViewer
import ru.tech.imageresizershrinker.presentation.root.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.presentation.root.widget.other.showError
import ru.tech.imageresizershrinker.presentation.root.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.presentation.root.widget.text.Marquee
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalWindowSizeClass
import java.io.DataInputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsScreen(
    type: Screen.PdfTools.Type?,
    onGoBack: () -> Unit,
    viewModel: PdfToolsViewModel = hiltViewModel()
) {
    LaunchedEffect(type) {
        type?.let { viewModel.setType(it) }
    }

    val context = LocalContext.current as Activity
    val toastHostState = LocalToastHost.current
    val scope = rememberCoroutineScope()
    val confettiController = LocalConfettiController.current

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val sizeClass = LocalWindowSizeClass.current.widthSizeClass
    val portrait =
        remember(
            LocalLifecycleOwner.current.lifecycle.observeAsState().value,
            sizeClass,
            configuration
        ) {
            derivedStateOf {
                configuration.orientation != Configuration.ORIENTATION_LANDSCAPE || sizeClass == WindowWidthSizeClass.Compact
            }
        }.value

    val onBack = {
        if (!viewModel.canGoBack()) showExitDialog = true
        else if (viewModel.pdfType != null) {
            viewModel.clearType()
        } else onGoBack()
    }

    val showConfetti: () -> Unit = {
        scope.launch {
            confettiController.showEmpty()
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument(),
        onResult = {
            it?.let { uri ->
                viewModel.savePdfTo(
                    outputStream = context.contentResolver.openOutputStream(uri, "rw")
                ) { t ->
                    if (t != null) {
                        scope.launch {
                            toastHostState.showError(context, t)
                        }
                    } else {
                        scope.launch {
                            confettiController.showEmpty()
                        }
                        scope.launch {
                            toastHostState.showToast(
                                context.getString(
                                    R.string.saved_to_without_filename,
                                    ""
                                ),
                                Icons.Rounded.Save
                            )
                            showReview(context)
                        }
                    }
                }
            }
        }
    )

    val pdfToImagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.setPdfToImagesUri(it)
            }
        }
    )

    val pdfPreviewPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.setPdfPreview(it)
            }
        }
    )

    val imagesToPdfPicker = rememberImagePicker(
        mode = localImagePickerMode(Picker.Multiple)
    ) { list ->
        list.takeIf { it.isNotEmpty() }?.let { uris ->
            viewModel.setImagesToPdf(uris)
        }
    }

    val addImagesToPdfPicker = rememberImagePicker(
        mode = localImagePickerMode(Picker.Multiple)
    ) { list ->
        list.takeIf { it.isNotEmpty() }?.let { uris ->
            viewModel.addImagesToPdf(uris)
        }
    }

    val focus = LocalFocusManager.current

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = topAppBarState)

    val shareButton: @Composable (pdfType: Screen.PdfTools.Type?) -> Unit = {
        val pdfType = it
        IconButton(
            onClick = {
                viewModel.preformSharing(showConfetti)
            },
            enabled = pdfType != null
        ) {
            Icon(Icons.Outlined.Share, null)
        }
    }

    val buttons: @Composable (pdfType: Screen.PdfTools.Type?) -> Unit = {
        val pdfType = it
        FloatingActionButton(
            onClick = {
                when (pdfType) {
                    is Screen.PdfTools.Type.ImagesToPdf -> {
                        imagesToPdfPicker.pickImage()
                    }

                    is Screen.PdfTools.Type.Preview -> {
                        pdfPreviewPicker.launch(arrayOf("application/pdf"))
                    }

                    else -> {
                        pdfToImagesPicker.launch(arrayOf("application/pdf"))
                    }
                }
            },
            modifier = Modifier.containerFabBorder(),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            Icon(
                imageVector = when (pdfType) {
                    is Screen.PdfTools.Type.ImagesToPdf -> Icons.Rounded.AddPhotoAlternate
                    else -> Icons.Rounded.FileOpen
                },
                contentDescription = null
            )
        }
        if (pdfType !is Screen.PdfTools.Type.Preview) {
            if (portrait) {
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            FloatingActionButton(
                onClick = {
                    if (pdfType is Screen.PdfTools.Type.ImagesToPdf && viewModel.imagesToPdfState != null) {
                        val name = viewModel.generatePdfFilename()
                        viewModel.convertImagesToPdf {
                            savePdfLauncher.launch("application/pdf#$name.pdf")
                        }
                    } else {
                        viewModel.savePdfToImage { savingPath ->
                            if (savingPath.isNotEmpty()) {
                                scope.launch {
                                    toastHostState.showToast(
                                        context.getString(
                                            R.string.saved_to_without_filename,
                                            savingPath
                                        ),
                                        Icons.Rounded.Save
                                    )
                                }
                                showConfetti()
                            }
                        }
                    }
                },
                modifier = Modifier.containerFabBorder(),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ) {
                Icon(Icons.Rounded.Save, null)
            }
        }
    }

    val controls: @Composable (pdfType: Screen.PdfTools.Type?) -> Unit = {
        val pdfType = it
        if (pdfType is Screen.PdfTools.Type.ImagesToPdf) {
            Column(Modifier.padding(20.dp)) {
                ImageReorderCarousel(
                    images = viewModel.imagesToPdfState,
                    onReorder = viewModel::reorderImagesToPdf,
                    onNeedToAddImage = { addImagesToPdfPicker.pickImage() },
                    onNeedToRemoveImageAt = viewModel::removeImageToPdfAt
                )
                Spacer(Modifier.height(8.dp))
                ScaleSmallImagesToLargeToggle(
                    selected = viewModel.scaleSmallImagesToLarge,
                    onCheckedChange = viewModel::toggleScaleSmallImagesToLarge
                )
            }
        }
    }

    Box {
        Surface(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focus.clearFocus()
                    }
                )
            },
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Column(Modifier.fillMaxSize()) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier.drawHorizontalStroke(),
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.pdfType,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) { pdfType ->
                                    Text(
                                        text = stringResource(pdfType?.title ?: R.string.pdf_tools),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            )
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (!portrait) {
                                shareButton(viewModel.pdfType)
                            }
                            TopAppBarEmoji()
                        }
                    )
                    val screenWidth = LocalConfiguration.current.screenWidthDp
                    val easing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)
                    AnimatedContent(
                        transitionSpec = {
                            if (targetState != null) {
                                slideInHorizontally(
                                    animationSpec = tween(600, easing = easing),
                                    initialOffsetX = { screenWidth }) + fadeIn(
                                    tween(300, 100)
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(600, easing = easing),
                                    targetOffsetX = { -screenWidth }) + fadeOut(
                                    tween(300, 100)
                                )
                            } else {
                                slideInHorizontally(
                                    animationSpec = tween(600, easing = easing),
                                    initialOffsetX = { -screenWidth }) + fadeIn(
                                    tween(300, 100)
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(600, easing = easing),
                                    targetOffsetX = { screenWidth }) + fadeOut(
                                    tween(300, 100)
                                )
                            }
                        },
                        targetState = viewModel.pdfType
                    ) { pdfType ->
                        when (pdfType) {
                            null -> {
                                val cutout = WindowInsets.displayCutout.asPaddingValues()
                                LazyVerticalStaggeredGrid(
                                    modifier = Modifier.weight(1f),
                                    columns = StaggeredGridCells.Adaptive(300.dp),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = 12.dp,
                                        alignment = Alignment.CenterHorizontally
                                    ),
                                    verticalItemSpacing = 12.dp,
                                    contentPadding = PaddingValues(
                                        bottom = 12.dp + WindowInsets
                                            .navigationBars
                                            .asPaddingValues()
                                            .calculateBottomPadding(),
                                        top = 12.dp,
                                        end = 12.dp + cutout.calculateEndPadding(
                                            LocalLayoutDirection.current
                                        ),
                                        start = 12.dp + cutout.calculateStartPadding(
                                            LocalLayoutDirection.current
                                        )
                                    ),
                                ) {
                                    item {
                                        PreferenceItem(
                                            onClick = {
                                                pdfPreviewPicker.launch(arrayOf("application/pdf"))
                                            },
                                            icon = Icons.Rounded.Preview,
                                            title = stringResource(R.string.preview_pdf),
                                            subtitle = stringResource(R.string.preview_pdf_sub),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    item {
                                        PreferenceItem(
                                            onClick = {
                                                pdfToImagesPicker.launch(arrayOf("application/pdf"))
                                            },
                                            icon = Icons.Rounded.Collections,
                                            title = stringResource(R.string.pdf_to_images),
                                            subtitle = stringResource(R.string.pdf_to_images_sub),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    item {
                                        PreferenceItem(
                                            onClick = {
                                                imagesToPdfPicker.pickImage()
                                            },
                                            icon = Icons.Rounded.PictureAsPdf,
                                            title = stringResource(R.string.images_to_pdf),
                                            subtitle = stringResource(R.string.images_to_pdf_sub),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            else -> {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .navBarsPaddingOnlyIfTheyAtTheEnd(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (pdfType is Screen.PdfTools.Type.Preview) {
                                            Box(
                                                Modifier
                                                    .container(
                                                        shape = RectangleShape,
                                                        resultPadding = 0.dp,
                                                        color = MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                    .weight(1.2f)
                                                    .clipToBounds(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                PdfViewer(uriState = pdfType.pdfUri)
                                            }
                                        }
                                        if (pdfType !is Screen.PdfTools.Type.Preview) {
                                            LazyColumn(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                contentPadding = PaddingValues(
                                                    bottom = WindowInsets
                                                        .navigationBars
                                                        .asPaddingValues()
                                                        .calculateBottomPadding() + WindowInsets.ime
                                                        .asPaddingValues()
                                                        .calculateBottomPadding(),
                                                ),
                                                modifier = Modifier
                                                    .weight(0.7f)
                                                    .fillMaxHeight()
                                                    .clipToBounds()
                                            ) {
                                                item {
                                                    controls(pdfType)
                                                }
                                            }
                                        }
                                        if (!portrait) {
                                            Column(
                                                Modifier
                                                    .container(
                                                        shape = RectangleShape,
                                                        color = MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                    .padding(horizontal = 20.dp)
                                                    .fillMaxHeight()
                                                    .navigationBarsPadding(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                buttons(pdfType)
                                            }
                                        }
                                    }
                                    if (portrait) {
                                        BottomAppBar(
                                            actions = {
                                                shareButton(pdfType)
                                            },
                                            floatingActionButton = {
                                                Row {
                                                    buttons(pdfType)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (viewModel.isSaving) {
                    if (viewModel.pdfType is Screen.PdfTools.Type.PdfToImages) {
                        LoadingDialog(viewModel.done, viewModel.pdfToImageState?.size ?: 1) {
                            viewModel.cancelSaving()
                        }
                    } else {
                        LoadingDialog {
                            viewModel.cancelSaving()
                        }
                    }
                }
            }
        }
    }

    ExitWithoutSavingDialog(
        onExit = {
            if (viewModel.pdfType != null) {
                viewModel.clearType()
            } else onGoBack()
        },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    BackHandler(onBack = onBack)
}

private class CreateDocument : ActivityResultContracts.CreateDocument("*/*") {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(
            context = context,
            input = input.split("#")[0]
        ).putExtra(Intent.EXTRA_TITLE, input.split("#")[1])
    }
}

private fun InputStream.toByteArray(): ByteArray {
    val bytes = ByteArray(this.available())
    val dis = DataInputStream(this)
    dis.readFully(bytes)
    return bytes
}