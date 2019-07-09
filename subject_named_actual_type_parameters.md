# `Subject`: Remove `named()`, `actual()`, and type parameters

***Note:*** The decisions proposed in this document have already been
implemented. We are publishing the document for any users who are interested in
our thought process.


## Background

### `Subject` has 2 type parameters

Truth's `Subject` class defines 2 type parameters:

```java
/**
 * ...
 *
 * @param <S> the self-type, allowing {@code this}-returning methods to avoid needing subclassing
 * @param <T> the type of the object being tested by this {@code Subject} [i.e., the actual value]
 * ...
 */
public class Subject<S extends Subject<S, T>, T> {
```

(See a footnote note on `<S>`[^1].)

Each type parameter has one purpose:

*   `S` is the return type of <code>named(...)</code>, our only
    <code>this</code>-returning method. Test writers use it like this:
    <br><code>assertThat(username).named("username").isNotEmpty();</code>
*   <code>T</code> is the return type of <code>actual()</code>, a
    <code>protected</code> method used by <code>Subject</code> implementations
    to retrieve the value under test. Subclasses use it in operations like this:
    <br>`if (!actual().containsEntry(key, value)) { fail(...); }`

This proposal (eventually, a few pages from now...) is to remove those type
parameters and so, necessarily, to remove the 2 methods that use them. (OK, it's
not _strictly_ necessary to remove the 2 methods, but we'll discuss that later.)

### Self-type parameters make subclassing hard

When you implement a `Subject`, you have to decide what to supply for the type
parameters. Your options:

#### 1. Specify concrete values for both type parameters

```java
class ThrowableSubject extends Subject<ThrowableSubject, Throwable>
```

This is the most popular and convenient option. The problem arises when someone
wants to subclass your subject:

a. Calling `named` on the subclass will return plain `ThrowableSubject` instead
of the subtype. The subclass can override named, but few do.

b. It's impossible to declare an appropriate `Subject.Factory` for the subtype.
At best, you can declare a factory that accepts any `Throwable` (which might or
might not be what you want) and returns a plain `ThrowableSubject` (unlikely to
be what you want). Both of these are problems that we have inside Truth, and so
do at least some other users. (And likely some other users wanted to make this
work but couldn't figure it out: The users who _are_ defining such a factory are
mostly doing so because I personally edited their code to define it.) Note that
defining a factory and casting isn't a convenient solution for users[^2], as any
non-`assert_()` users of the subject (like `check()` or `expectFailure()`) have
to ensure they pass the right argument type and cast the result.

I _think_ we could half solve (b) by loosening the generics of `Subject.Factory`
from:

```java
<SubjectT extends Subject<SubjectT, ActualT>, ActualT>
```

to:

```java
<SubjectT extends Subject<SubjectT, ?>, ActualT>
```

This would permit declaring a `Subject.Factory` that requires a more specific
type for the actual value (e.g., `Multiset` instead of `Iterable`). However, it
would still return the original subject type (`IterableSubject`, rather than
`MultisetSubject`). And the subclass would also need to override `named` to
solve (a).

#### 2. Declare your own `<S, T>` parameters

```java
class ComparableSubject<S extends ComparableSubject<S, T>, T extends Comparable>
    extends Subject<S, T>
```

This is the most flexible option (arguably, the _correct_ option for extensible
subjects), but:

a. It's a mouthful. And keep in mind that subjects may have their own type
parameters:

```java
public abstract class TransformedValueSubject<
        S extends TransformedValueSubject<S, D, V, O>,
        D extends OriginalValueSubject.ValueDescription,
        V extends Value,
        O extends OriginalValueSubject<O, D, V>>
    extends TransformedSubject<S, D, V, O> {
```

Compare that to a version without `<S, D>` (and, as a result, also without
`<V>`):

```java
public abstract class TransformedValueSubject<O extends OriginalValueSubject>
    extends TransformedSubject<O> {
```

b. It's not possible to define a `Subject.Factory` for the type itself. If you
want users to be able to create a plain instance of your type, say,
<code>ViewSubject</code>, then you need to declare an
<code>AbstractViewSubject</code> with the type parameters <em>plus</em> a
<code>ViewSubject</code> that extends
<code>AbstractViewSubject<ViewSubject, View></code>. If you do, the users see
two types, with the assertions defined on a different type than the one they
have an instance of[^3].

#### 3. A hybrid option, where you specify a concrete actual-value type but declare a self-type parameter

```java
class AbstractCheckedFutureSubject
        <S extends [AbstractCheckedFuture]Subject<S, CheckedFuture<?, ?>>>
    extends Subject<S, CheckedFuture<?, ?>>
```

This works well if all your subclasses are happy to accept a plain
`CheckedFuture` (rather than having some subclasses that need to require a
specific subclass of that); otherwise, not. Of course, it's a mouthful, too. And
you still need a non-`abstract` subclass if you want users to be able to create
an instance of this plain type, as in 2(b).

#### 4. Give up on extension

Maybe you give up on extending `IterableSubject` (or letting people extend your
custom subject) entirely, or you ask for advice, or you realize on your own that
you can delegate to `IterableSubject` instead of extending it, implementing
methods wholesale or as needed.

Delegation is especially common with `ThrowableSubject`.

We see the `IterableSubject` problem in our own ProtoTruth (though the situation
there is more complex).

Now, normally we favor composition over inheritance. However, this is a bit of a
different case: Just as `MyException` extends `Throwable`, it's generally
reasonable for `MyExceptionSubject` to extend `ThrowableSubject` (if it weren't
blocked by these generics issues). And extension has advantages: Custom subjects
pick up new methods from the superclass automatically, and they're covered by
any static analysis that finds issues like type mismatches. Plus, any custom
methods added by the custom subject stand out from the default `IterableSubject`
methods, which are defined in another file. Additionally, it's (usually; there
are other edge cases) possible to import a new `assertThat` method without
breaking existing code, since `assertThat(SubFoo)` is likely to expose all the
same assertions as `assertThat(Foo)`.

(It is still reasonable for some subjects to _choose_ not to extend an existing
subject type, perhaps to limit the number of assertions they expose to a more
tractable set. (For example, ProtoTruth doesn't want to expose the no-arg
`isInOrder`, since proto classes don't define a natural order.) I'd just like
for them to have a choice.)

### Truth offers multiple ways to add to failure messages

There are a few, and there are likely to be more someday.

Truth currently provides 2 ways for the caller of an assertion to add to the
assertion's default failure message:

*   <code>assertThat(username).<strong>named("username")</strong>.isNotEmpty();</code>
*   <strong><code>assertWithMessage("username").that(username).isNotEmpty();</code></strong>

Under the old, prose-style failure messages, the messages these produce looked
significantly different. Under the new, key-value-style failure messages, they
look almost the same: Both put "username" on a new line at the beginning of the
message. The only difference is that <code>named</code> prefixes it with "name:
."

In addition to those 2 ways, we recently added another way tailor-made for the
specific case in which one assertion is being implemented in terms of another:

*   <code><strong>check("username()")</strong>.that(actual().username()).isNotEmpty();</code>

(Note that <em>implementations</em> of <code>Subject</code> classes naturally
have influence over the failure message in other ways, thanks to other methods
they can call and implement. I mention only <code>check("username")</code> here
because it's the most similar to <code>named</code> and
<code>assertWithMessage</code>, and in fact people used <code>named</code> in
place of <code>check("username")</code> before the latter existed. But keep in
mind that there are plenty of existing options and future possibilities here,
too[^4].)

On top of the existing ways for callers to add to the failure message, it's
likely that we'll add some others in the future. For example, we've had several
requests for adding context or scoping assertions. We've also speculated about a
`Fact`-based method.

You could even argue that we have some other ways of supplying messages, like
<code>assert_().fail(...)</code> and maybe someday
<code>Truth.fail(...)</code> -- and maybe even an <code>assertThrows</code>
someday. And hopefully we'll soon automatically infer a good description.

### Some subjects fail to include the name passed to `named` in their failure messages

About half(!) of custom assertion methods omit it, and so do some assertions in
Truth itself. The usual cause is a call to the no-arg check() method. These
should someday be fixed, but I have automation for only about half the work, and
we may need to add new APIs to support some callers, so the rest won't happen
anytime soon. (Other assertions drop _all_ context, but that is easier to fix.)

Also note that, for most subjects that have subclasses, `named` doesn't return
the right type on the subclasses, thanks to the generics issues described above.

## Issue A: Remove `named`

Users would use `assertWithMessage` (or `withMessage`) instead.

*   \+ Removing it is (almost) a prerequisite to simplifying type parameters.
    *   To be clear: This is my primary motivation (though the next bullet,
        about how `named` often doesn't work with custom subjects, is fairly
        embarrassing, too).
    *   For more on the advantages of simplifying type parameters, see Issue C.
    *   I say "almost" a prerequisite because we could keep `named` but have it
        return a plain `Subject`.
        *   That would mean that you couldn't write things like
            `assertThat(string).named(...).contains(...)`, only things like
            `assertThat(string).named(...).isEqualTo(...)`.
        *   We could then "fix" that by overriding `named` in all our `Subject`
            subclasses, but it seems very unlikely that most _custom_ subjects
            would do that (since presumably they don't view `named` as an
            essential feature).
        *   We could try to force custom subjects to do it by making `named` be
            `abstract`, but:
            *   The boilerplate would annoy people.
            *   That wouldn't solve the problem for subclasses of custom
                subjects.
*   \+ Some subjects fail to include the name passed to named in their failure
    messages.
    *   To be fair, most assertions use the built-in subjects (or extensions
        that we own), which we can ensure get this right (though we don't always
        get it right currently).
    *   And we're phasing no-arg `check()` calls out, anyway.
    *   However, it's possible (though far from certain) that we'll someday
        introduce some kind of "check, replacing this value in the chain with
        the new value." `named` would require special handling there, though we
        could probably make it work.
    *   I think `named` is also lost if it's called in the middle of a chain,
        even if it uses the overload of `check` that accepts arguments. Possibly
        we could avoid that, but if the user has `foo.bar.baz` and calls `named`
        on the subjects for both `foo` and `foo.bar.baz`, what should the
        failure message be? "value of: the baz, aka the foo.bar.baz?"
*   \+ All else equal, 1 way to add context is better than 2 (or n is better
    than n+1).
    *   It's less for users to understand and less for us to document. (In
        particular, it's one less method on `Subject`, a type that's already
        crowded with methods for both users and subclasses.)
    *   It's less for static analysis to handle. (For example, `TruthSelfEquals`
        missed some bugs because of `named`. For more examples, see the next
        bullet about `named`.)
    *   It's less to maintain (bug fixes, feature requests).
    *   And `assertWithMessage` has 4x as many users as `named`, so (again, all
        else being equal), `named` is the one to get rid of. It also has faster
        growth, even in relative terms (>6% vs. <3% over the last ~4 months.)
*   \+ We have just a shred of evidence that users sometimes think of "named" as
    an assertion, so they write "assertions" like
    `assertThat(description).named("expected-description");`.
    *   A similar problem appears to be with "assertions" of the form
        `assertThat(someBoolean).named("something");`.
    *   Or, occasionally, users appear to just forget to write the assertion
        after filling in the name. (Perhaps that's easier to do when you've
        written `assertThat(foo).named(...)` than when you've written
        `assertWithMessage(...)` (which doesn't even include the actual value),
        and perhaps it stands out less, too?)
    *   But both of these ought to be caught by `@CheckReturnValue` (like a
        plain `assertThat(someBoolean);` "assertion" is); we just never got
        around to removing `@CanIgnoreReturnValue` from `named`.
    *   Actually, wait, that's not quite true: Because `named` is a mutator,
        it's actually "fine" to call `named` on an object and ignore the return
        value. For example, we have several callers who call `[this.]named(...)`
        in their constructors. We also see it called on delegate subjects.
    *   Still, this is at least a small negative, especially for users without
        `@CheckReturnValue` enforcement.
*   \+ This also allows us to get rid of `actualAsString()` and
    `internalCustomName()`.
*   \+ It's the one thing that makes `Subject` instances mutable.
    *   Admittedly, I don't know of any concrete problems that this has
        caused -- except arguably for discouraging us from enabling
        `CheckReturnValue` enforcement, as described in an earlier bullet.
    *   Because some `Subject` implementations call `named` internally (either
        `this.named(...)` or `check(....).that(...).named(...)` (or
        `assertThat(...).named(...)` if they haven't been moved to `check` :)),
        it's possible that their name gets overwritten by a later user-specified
        name. I haven't seen this come up, but then I haven't looked at all
        users of the possibly affected `Subject` implementations. We could in
        principle fix this by "stacking" multiple `named` calls (like what we do
        with multiple `withMessage` calls) (so this bullet isn't really about
        mutability per se; it just didn't feel important enough for a top-level
        bullet), but it's more work we should do if we keep things as they are.
*   ~ The policy for when to include `named` in the failure message has changed
    over time and may be confusing.
    *   The old `fail*` methods mostly included it, including (perhaps
        surprisingly) `failWithoutActual`/`failWithoutSubject` but _not_
        (probably _un_surprisingly but still likely to bite someone)
        `failWithRawMessage`. (`failWithRawMessage` used to be another very
        common case in which `named` was ignored.) However, we changed
        `failWithRawMessage` (and its modern equivalent) to include the name.
        This is probably good overall, but it also means that `named` (and
        `assertWithMessage`, if we migrate `named` users to that) may be
        duplicating information that was manually added by the call to
        `failWithRawMessage`.
*   ~ I see at least some evidence that people are filling in `named` even in
    cases in which Truth already includes the information they're providing.
    *   As an example, several people are passing names like "Exception message"
        to assertions that will already explain that.
*   ~ It's possible that `named` and `withMessage` encourage different kinds of
    messages.
    *   Maybe users are more willing to use a short description of the actual
        value for `named` than for `withMessage`? (I haven't tried to
        investigate.)
        *   Maybe it's good to encourage short names?
            *   There's no sense in writing "The foo was expected to be null"
                when you can communicate the same information by writing "foo."
            *   If engineers don't feel obligated to write long descriptions,
                maybe they'll write more?
        *   Maybe it's bad?
            *   Messages that give additional information (like the values of
                variables or the reason that the test exists) are more useful
                than names that just save you a trip to the test source file.
*   ~ Some people may find that some assertion statements read more naturally
    with the name inline -- or, conversely, with the message out-of-line.
*   ~ AssertJ has a method on its `Subject`-like class like this.
    *   Though it's displayed slightly more like our `withMessage` (which is a
        method on `StandardSubjectBuilder`, not on `Subject`) than our `named`.
        (Their withFailMessage _replaces_ the failure message, rather than
        adding to it.)
    *   \- So, if we leave it out, this may slightly complicate migration from
        AssertJ to Truth.
    *   But we don't have to follow AssertJ (nor FEST, which was probably our
        inspiration).
    *   Note also that AssertJ more strongly encourages proper self-typing by
        (a) providing both `FooSubject` and <code>AbstractFooSubject</code> in
        all(?) cases and (b) accepting a `Class<S>` parameter in their
        Subject-like class's constructor.
*   ~ If a particular custom <code>Subject</code> wants to support this, it can
    add its own.
    *   Of course we would feel bad if users chose to do this <em>often</em>,
        but if it were particularly important somewhere, it's an option (and
        maybe there's even a way they can make it better for their specific use
        case??).
*   ~ Since we'd migrate calls from <code>named</code> to
    <code>assertWithMessage</code>, it's possible that we'll produce some less
    than ideally phrased failure messages.
    *   However, the real damage here was already done when we changed the
        format of our failure messages: That's what moved the given name from
        inline ("Not true that my object (<...>) was true") to a separate line,
        prefixed by "named:." The main thing we're doing now is removing that
        "named" prefix.
*   \- Requires an LSC to migrate existing users to
    <code>assertWithMessage</code> (or, if we're up for it, sometimes to
    <code>check(String, Object...)</code>).
    *   This can be done with Refaster for the common cases but requires Error
        Prone for full generality. It's doable.
    *   This includes some Kotlin.
*   \- Will break external users' code.
    *   We'll release Error-Prone-powered migration tools, but users would need
        to run them, and that requires some up-front investment to set up Error
        Prone.
    *   \(Or, as an easy "fix," users can just remove <code>named</code> calls.)
*   \- <code>named</code> is more convenient for users of custom subjects.
    *   Large caveat: That's only true when it works. (And see also the cases in
        which <code>named</code> doesn't return the original subject type.)
    *   But, when it does work, compare:
    *   <code>assertThat(foo).named("foo").hasBar(bar);</code>
    *   <code>assertWithMessage("foo").about(foos()).that(foo).hasBar(bar);</code>
    *   (And that's assuming that the author of <code>FooSubject</code> exposed
        a <code>Subject.Factory</code> -- which all should (since the lack of
        one causes problems other than this) but not all do. (I don't have
        numbers, but I'd guess that a majority do. We should write Error Prone
        checks for this.))
        *   This makes editing an existing assertion to add a message more
            complex.
        *   It may discourage users from adding messages at all.
*   \- Maybe people like the appearance of having every assertion begin with
    <code>assertThat(</code>?
    *   But that's already not the case:
        *   <code>assertWithMessage</code> exists (and is a better fit than
            <code>named</code> in some cases, as <code>named</code> is intended
            only for naming a specific value, not for giving additional
            background like the values of other variables or the reason that the
            test exists).
        *   So do <code>expect</code> and other alternative failure strategies,
            which sometimes require <code>expect.about(...).that(...)</code>
            instead of <code>expect.that(...)</code>.

Another way that people may be interested in looking at this: What has changed
since we originally added <code>named</code>? <code>named</code> was added in
version 0.17 in 2014 before Truth became a Google project developed in our
depot, so it didn't go through API Review, but it's still useful to consider
what's changed:

*   We've added `assertWithMessage` and `withMessage` (originally named
    `withFailureMessage` but subsequently shortened), which offer similar
    functionality.
    *   (Note again that `named` behaves almost identically with `withMessage`
        under the new failure message style. That wasn't the case with the
        failure-message style we were using when `withMessage` was first added.)
*   We've added `check(String, Object...)`, which is superior to
    <code>check().that(...).named(...)</code>.
*   We should soon automatically infer a description of the actual value in some
    cases. Hopefully this (and other changes, like to always include the
    (now-trimmed) stack trace when using <code>Expect</code>) will address
    common use cases for names and custom messages, including boolean
    assertions.
*   We have more static analysis (which <code>named</code> complicates), more
    custom subjects (which often don't work right with <code>named</code>), and
    more users (to be bit by the preceding problems).
*   Additionally, the <code>named</code> functionality seems to have come from
    FEST), which, besides not offering <code>withMessage</code>, already has a
    self-type parameter for chaining multiple assertions on the same value, as
    in <code>assertThat(x).isNotNull().isNotEqualTo(other).contains(x)</code>.
    So even if FEST dropped its equivalent to <code>named</code>, that wouldn't
    permit it to remove its self-type parameter.

## Issue B: Remove <code>actual</code>

Each subclass would have to declare a field of the appropriate type and store
the actual value during its constructor. (It's legal Java for every class in a
hierarchy to declare a `private` field named `actual`.)

```java
+  @Nullable private final Integer actual;
+
   protected IntegerSubject(FailureMetadata metadata, @Nullable Integer integer) {
     super(metadata, integer);
+    this.actual = integer;
   }
```

*   \+ Removing it is (almost) a prerequisite to simplifying type parameters.
    *   This is my primary motivation (like in Issue A, only more so).
    *   For more on the advantages of simplifying type parameters, see Issue C.
    *   I say "almost" a prerequisite because, if we really wanted to remove the
        type parameters but keep `actual`, we could do it by changing `actual`
        to return `Object`.
        *   Subclasses could then use it by casting to the appropriate type.
            *   Casts are probably more error-prone (and disliked) than fields.
        *   The casts would be unchecked in some cases.
        *   We could also make it overridable so that subclasses can redefine it
            to return the appropriate type.
            *   But they might think they can do weird things like effectively
                change the actual value by overriding the method, which might or
                might not work, depending on the implementation of each any
                individual assertion.
                *   We saw something like this when a user overrode one of the
                    `fail*` methods.
            *   And an override makes `actual` visible to users of the subject
                in the same package.
        *   I don't think anyone really wants this; the value of `actual` is
            primarily as a convenient way to return a _typed_ value.
            *   (This does save a field, if anyone cares about that. But Truth
                already does more inefficient things than have multiple `actual`
                fields.)
            *   We could look into how many subclasses actually need the typed
                actual value, not just `Object`. We suspect that most do.
        *   Another note: Removing _only one_ type parameter from `Subject` is
            harder than removing _both_.
            *   That's because removing one would have to be done atomically,
                while removing both can be done by gradually making all subjects
                extend raw `Subject` and later removing the type parameters.
            *   So, if we want to remove `named` and the pseudo-self-type
                parameter, then it's simplest to remove `actual` and its type
                parameter, too.
            *   If not for that, I'd _consider_ keeping this method and type
                parameter in place (along with loosening the type parameters of
                `Subject.Factory`, as discussed above).
*   \+ It removes one of the subclass-facing methods from the crowded `Subject`
    type, which users look at primarily for its assertion methods.
    *   To be fair, it's a `protected` method, anyway, so, while users see it in
        source and Javadoc, they don't see it in autocomplete.
    *   And there are several other methods like this, including others
        beginning with the word actual.
*   \+ This may help static analysis, like nullness analysis, which can
    recognize that a `final` field won't change between reads but has a harder
    time recognizing that a method (declared in a separate file) won't return
    different values between calls.
    *   Example: <code>if (actual() == null || actual().doubleValue() != 0.0)
        {</code>
    *   (Note that nullness in the type system doesn't necessarily avoid this
        problem: We might well want to let users write
        <code>assertThat(possiblyNullDouble).isZero()</code> without first
        performing their own null check. Or maybe not; it's not clear that users
        will have a universal preference here, nor is it clear whether we can
        support any given behavior, let alone a configurable one. It's possible
        that full support for nullabililty (whatever that means) would require
        including the actual-value type parameter on <em>all</em> classes, even
        <code>final</code> ones, <em>and</em> on all
        <code>Subject.Factory</code> and <code>assertThat</code> methods, to
        distinguish between "a <code>Subject</code> for a <code>Foo</code>" and
        "a <code>Subject</code> for a <code>Foo</code> or <code>null</code>.")
*   \+ Each usage of the actual value is <em>a whole two characters
    shorter</em> :)
*   ~ Subclasses could choose a different name than <code>actual</code> for
    their fields.
    *   \+ This could be helpful if users need to distinguish between one
        view/part of the actual value and another (like how our
        <code>StreamSubject</code> has an <code>actualList</code> that it uses
        for its <code>stream</code>-specific operations).
    *   \- This could be less clear to readers than sticking with the
        convention.
*   ~ We recently heard from some Kotlin users of Truth who are reflectively
    calling <code>actual()</code> as part of implementing extension methods on
    subjects. They would have to reflectively access the
    <code>Subject.actual</code> <em>field</em> that will still exist. Downsides
    for them:
    *   <code>actual()</code>, while <code>protected</code> and not
        <code>public</code> (hence the need for reflection), is at least an API
        exposed to other packages. The <code>actual</code> field, by contrast,
        would be <code>private</code>.
        *   This might not matter in practice -- would we really rename or
            remove the field? -- but would at least feel icky.
        *   But I expect that we'll provide a real, public method for this
            someday -- say,
            <code>ExtensionMethods.getActualValue(Subject)</code>, backed by a
            package-private <code>actual()</code> accessor.
    *   The field type would be plain <code>Object</code>, rather than
        <code>T</code>.
        *   (Strictly speaking, that's more a result of removing the type
            parameters (Issue C), not of removing <code>actual</code>.)
        *   The same would be true of the return type of the hypothetical
            <code>ExtensionMethods.getActualValue(Subject)</code>.
        *   So callers would have to cast, and sometimes the cast would be
            unchecked.
        *   That's probably tolerable for the unusual case of extension methods,
            but it's not ideal.
        *   (Crazy workaround that I'm not sure I'd actually want: Let callers
            pass a <code>Subject.Factory</code> for the <code>Subject</code>
            itself as a type hint. But not all subjects have one (e.g.,
            ProtoTruth has a <code>CustomSubjectBuilder.Factory</code>), and
            anyway, it's kind of weird to use the factory as a typing hint --
            especially when another factory may well be involved for creating a
            derived subject.)
*   ~ It looks a little weird to some of us nowadays to see a constructor that
    assigns a parameter to a field without calling <code>checkNotNull</code>
    first :)
*   ~ For whatever reason, ~10% of <code>Subject</code> implementations already
    call <code>actual()</code> and assign the result to a local variable inside
    an assertion method.
*   \- Without <code>actual</code>, subclasses need 2 lines of boilerplate to
    manually store the actual value.
    *   The hope is that this is offset somewhat by removing the type
        parameters. In rare cases, removing the type parameters could even help
        more than it hurts. But on average, boilerplate would increase.
    *   Comparison: Consider that AssertJ (like Truth <em>currently</em>)
        doesn't require those 2 lines, as it has a <code>protected actual
        field</code>. (It does require the type parameters.)
*   \- It also just <em>feels weird</em> for the subclass, which <em>knows</em>
    that the superclass is storing <em>exactly</em> the value that it needs, to
    refuse to expose it.
    *   Counterargument from the similar case of injecting dependencies into a
        constructor that calls a super-constructor: Some people argue that, if
        the subclass needs a value that the superclass also needs, it's still
        preferable to <em>both</em> pass the value to <code>super()</code>
        <em>and also</em> store a local copy. This avoids tying the two classes
        closely together. So, for example, if the superclass no longer needs a
        parameter, it doesn't need to continue to store it and expose an
        accessor for it (or, alternatively, force the subclass to migrate off
        the accessor when it removes it).
*   \- Static analysis that looks for operations on the actual value will need
    more logic to detect each class's <code>actual</code> field (rather than the
    standard <code>actual()</code> method).
*   \- Requires an LSC to migrate existing users to declare and use a field.
    *   This again requires Error Prone, but it's straightforward.
    *   This again includes some Kotlin.
*   \- Will break external users' code.
    *   We'll release Error-Prone-powered migration tools, but users would need
        to run them, and that requires some up-front investment to set up Error
        Prone.
    *   It's also pretty straightforward to do by hand.
    *   "Hopefully" not too many people are writing custom subjects externally.
    *   We are planning to make a 1.0, after all.

## Issue C: Remove type parameters

I propose to remove both parameters. (Removing only one is much more difficult,
as discussed above.)

To re-reiterate: This is the primary goal of all the proposals in this doc.

*   \+ Self-type parameters make subclassing hard.
    *   Removing the type parameters also addresses the issue that _actual_-type
        parameters make subclassing hard, too, though that one could be solved
        in other ways.
    *   (Subclassing difficulties could be particularly bad if we ever explore
        codegen for subjects (example), which would likely be implemented with
        subclassing, like `AutoValue`.)
*   \+ Simpler for users of subjects.
    *   That's true in various situations:
        *   reading the code
        *   looking at the Javadoc
        *   interpreting compilation errors
    *   See again an extreme example above.
    *   This is another area where we can differentiate ourselves from AssertJ,
        which users have told us is more complex than Truth.
*   \+ Opens the door to re-adding element-type parameters to `IterableSubject`,
    etc.
    *   (Adding a type parameter _now_, when we already have type parameters, is
        hard, as discussed above. Plus, 3 type parameters (maybe more for types
        like `MultimapSubject` and `TableSubject`) looks especially scary,
        particularly when they don't map directly to the type parameters of the
        underlying actual-value type.)
    *   Benefits:
        *   This can support static analysis, like how Error Prone looks for
            type mismatches in calls to `Collections.contains`.
        *   This could provide better compile-time type-checking for users of
            `isInOrder`.
        *   This could provide better compile-time type-checking for users of
            Fuzzy Truth.
        *   This could provide better type inference for users of Fuzzy Truth.
    *   However, we're not deciding on this yet:
        *   It's not a 1.0 blocker, as the parameters are safe to add later.
            *   (The possible `isInOrder` and Fuzzy Truth changes are
                binary-compatible but not necessarily source-compatible. We'd
                have to take that into account. But even if we don't make those
                changes, we can still benefit from improved static analysis.)
        *   We'd want to check whether it would still be safe to static import
            both `Truth.assertThat(Iterable)` and
            `ProtoTruth.assertThat(Iterable)`. I think we currently get away
            with statically importing both because only ProtoTruth has a type
            parameter? Or maybe it would still work as long as ProtoTruth's
            parameter is more specific (which it will be)?
*   \+ Makes it a no-brainer to remove <code>DefaultSubject</code>.
*   \+ Should let us eliminate <code>LiteProtoSubject.Factory</code> as a public
    type.
*   \+ Should make it possible to create an API like
    <code>assertThat(future).value(strings()).startsWith("foo")</code>, should
    we want that someday.
*   ~ (Almost) requires removing <code>named</code> and <code>actual</code>, as
    discussed in previous issues.
*   \- Requires an LSC to remove the type parameters from existing subclasses.
    *   This should mostly be doable even with sed.
    *   For extra expediency, we could initially keep any self-type and
        actual-type parameters declared in custom subjects. (For example, `class
        MySubject<S extends MySubject<S, A>, A> extends Subject<S, A>` could
        continue to declare `<S ..., A>`, even though we'd switch it to extend
        unparameterized <code>Subject</code>. We could clean up the unused type
        parameters later.)
    *   This again includes some Kotlin.
*   \- Will break external users' code.
    *   "Hopefully" not too many people are writing custom subjects externally.
    *   And this one is mostly fixable with <code>sed</code>.
    *   This change is at least binary-compatible, just not source-compatible.
*   \- Cuts off other potential uses of self types.
    *   For example, AssertJ offers <code>inHexadecimal()</code> and
        <code>inBinary()</code>.
        *   However, I don't think we've seen demand for features like this in
            our years of maintaining Truth. (I didn't find any feature requests
            for hex or binary specifically, though I could believe they've come
            up.)
    *   AssertJ also defines most assertion methods to return the `Subject`,
        enabling chains like
        <code>assertThat(x).isNotNull().isNotEqualTo(other).contains(x)</code>,
        which we've decided not to support.
    *   AssertJ also offers <code>Predicate</code>-accepting methods, which can
        be type-safe with a self type.
        *   We have seen requests for this, and we plan not to implement it.
            However, it's possible that we'd consider something based on
            Correspondence someday.
    *   We have also seen requests for <code>Comparator</code>-based
        comparisons, which would be only half type-safe without the actual-value
        parameter.
    *   Even if there are problems that self types can solve, we may be able to
        find alternative solutions:
        *   We could still add <code>IntegerSubject.inHexadecimal()</code>,
            though it would return a plain <code>IntegerSubject</code>. That
            could be awkward if someone subclassed it, but:
            *   No one does currently internally.
            *   The <code>IntegerSubject</code> assertions will still be
                available, even if the subclass ones aren't, and that might
                often be good enough.
            *   A subclass could override <code>inHexadecimal()</code> to
                declare a more specific return type.
        *   We could automatically display values in hexadecimal/binary for any
            assertions we add for which that would be appropriate, like bitwise
            assertions. (For bitwise assertions, maybe we could do even better
            and figure out the names of the constants in some cases??)
        *   We can offer features in non-<code>Subject</code> classes, like
            <code>CaseInsensitiveStringComparison</code>. This doesn't really
            <em>solve</em> the problem, but it makes clear that, if you define a
            subclass, the <code>ignoringCase()</code> method is still going to
            return a plain <code>CaseInsensitiveStringComparison</code>, not
            some subtype of yours, unless you explicitly override it.

Again, it may be useful to review what has changed in the past several years.
That includes considering how Truth is different from FEST (which seems to have
been our inspiration for the type parameters). FEST uses the self type for
chaining multiple assertions on the same value, as in
<code>assertThat(x).isNotNull().isNotEqualTo(other).contains(x)</code>; Truth
does not. So even if FEST dropped its equivalent to <code>named</code>, that
wouldn't permit it to remove its self-type parameter. Truth has a better case
for dropping it, and one of the original authors had considered doing so (though
dropped the effort for reasons I'm unsure of -- perhaps just that we had more
pressing issues).

[^1]: "Avoid needing subclassing" might not be the best way to put this. The
    point is that we can declare a method that returns <code>S</code>, and we
    need to implement it only a single time in <code>Subject</code> itself.

[^2]: self-nitpicking: OK, it's also the type of the constructor parameter that
    subclasses call to pass that actual value to `Subject`. However, `Subject`
    uses the actual value only for assertions like `isNotNull()`, where it
    doesn't need to know that it's specifically a `T`. The `T` type
    specifically is in service of `actual()`, which is in service of
    subclasses.

[^3]: It turns out that a full (OK, _almost_ full) solution does exist. It
    hadn't occurred to me, but some users found it. The solution is to use
    <code>CustomSubjectBuilder</code>. This lets you force the input to be of
    whatever type you want and lets you return whatever subject type you want.
    In short, because you can't write:

    ```java
    public static Factory<FooSubject, Foo> foos() {
      return FooSubject::new;
    }
    ```

    You instead write:

    ```java
    public static CustomSubjectBuilder.Factory<FooSubjectBuilder> foos() {
      return FooSubjectBuilder::new;
    }

    public static final class FooSubjectBuilder extends CustomSubjectBuilder {
      FooSubjectBuilder(FailureMetadata metadata) {
        super(metadata);
      }

      public FooSubject that(Foo actual) {
        return new FooSubject(metadata(), actual);
      }
    }
    ```

    I am simultaneously horrified that this is necessary, impressed that some
    people found it, and tickled that `CustomSubjectBuilder` ended up
    satisfying this unforeseen use case.

[^4]: You could also make <code>ViewSubject</code> the abstract type. Then you'd
    create a private <code>ConcreteViewSubject</code> subtype and a factory
    for the subtype. I <em>think</em> this will work, but you'll have to
    expose the private <code>ConcreteViewSubject</code> type in the public
    <code>views()</code> method that exposes your factory, and then your
    <code>assertThat</code> method will either have to expose it again or else
    declare a return type of <code>ViewSubject<?, ?></code>, which is a little
    weird in its own way. (And it gets worse if you have other type parameters
    that you want to survive a call to <code>named</code>, like what we used
    to have on IterableSubject.)
