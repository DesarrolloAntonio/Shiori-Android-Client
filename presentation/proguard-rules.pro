# R8 rules for the release build.
#
# Only what has been shown to be necessary. Every rule below was added because the shrunk app
# actually failed without it, on a device, with the failure recorded next to the rule. Rules added
# speculatively are how a shrinker ends up keeping everything.
#
# Most of the stack needs nothing here: Retrofit, OkHttp, Room, Coil, Koin and Compose all ship
# their own consumer rules.

# Keep enough of a stack trace to be able to read a crash report. Without this a release stack is
# line numbers against obfuscated names and the mapping file is the only way back.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Protobuf lite, used by the proto DataStore that stores the session, the token and the user's
# preferences.
#
# GeneratedMessageLite reads and writes its fields reflectively by their generated names, so
# renaming them breaks it at runtime and not at build time. Observed on a Pixel Fold, the app
# dying on launch before drawing anything:
#
#   java.lang.RuntimeException: Field makeArchivePublic_ for yd3 not found. Known fields are
#   [public boolean yd3.e, public boolean yd3.f, public boolean yd3.g, ...]
#
# makeArchivePublic_ is a field of the generated preferences message. R8 had renamed it to e.
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Gson maps these by field name, so renaming the fields silently produces nulls.
#
# The model classes carry no @SerializedName at all, and the DTOs annotate only 4 of their fields;
# the rest rely on the name matching the JSON key. Nothing fails at build time: the object comes
# back with every unmatched field null, and a non-null Kotlin property holding null blows up
# somewhere far away.
#
# Observed on a device: the feed died in Compose text layout, three frames deep in a FlowRow,
#
#   java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()'
#   at androidx.compose.ui.text.AndroidParagraphIntrinsics.getMayHaveNewLine$ui_text
#
# which was a tag chip drawing Tag.name. Tag round trips through Gson inside TagsConverter, Room's
# type converter for the tags column, so every tag in the local database came back nameless.
#
# The DTOs need the same treatment for a second reason: Mapper.toEditBookmarkJson filters the edit
# payload with a Gson ExclusionStrategy that compares f.name against the literals "hasEbook" and
# "createEbook". Renamed fields never match, the strategy quietly excludes nothing, and the edit
# request carries fields the server does not expect.
-keep class com.desarrollodroide.model.** { *; }
-keep class com.desarrollodroide.network.model.** { *; }

# TagsConverter resolves List<Tag> through a TypeToken. Without the generic signature Gson cannot
# tell what the list holds and hands back LinkedTreeMaps.
-keepattributes Signature
-keepattributes *Annotation*

# WorkManager builds the input merger named in the WorkSpec by reflection, so the no-argument
# constructor has to survive even though nothing calls it.
#
# Nothing crashes when it does not. The worker simply never runs, and the only trace is in logcat:
#
#   E WM-InputMerger: Trouble instantiating androidx.work.OverwritingInputMerger
#   E WM-InputMerger: java.lang.NoSuchMethodException: androidx.work.OverwritingInputMerger.<init> []
#   E WM-WorkerWrapper: Could not create Input Merger androidx.work.OverwritingInputMerger
#
# Observed by adding a bookmark on the shrunk build: the row appeared locally with its temporary id
# and the pending banner, the CREATE was queued, and it never reached the server. The same URL
# added from the unshrunk build POSTed and came back with a real id.
-keep class * extends androidx.work.InputMerger { <init>(); }
