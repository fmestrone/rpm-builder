package de.dentrassi.rpm.builder;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.packagedrone.utils.rpm.build.BuilderContext;
import org.eclipse.packagedrone.utils.rpm.build.FileInformation;
import org.eclipse.packagedrone.utils.rpm.build.FileInformationProvider;
import org.eclipse.packagedrone.utils.rpm.build.PayloadEntryType;

public class MojoFileInformationProvider implements FileInformationProvider<Object>
{
    private final RulesetEvaluator rulesetEval;

    private final Consumer<String> logger;

    private final String ruleId;

    private final PackageEntry entry;

    public MojoFileInformationProvider ( final RulesetEvaluator rulesetEval, final String ruleId, final PackageEntry entry, final Consumer<String> logger )
    {
        this.rulesetEval = Objects.requireNonNull ( rulesetEval );
        this.ruleId = ruleId;
        this.entry = entry;
        this.logger = logger != null ? logger : ( s ) -> {
        };
    }

    @Override
    public FileInformation provide ( final Object object, final PayloadEntryType type ) throws IOException
    {
        final FileInformation result = provideByRule ( object, type );

        if ( result == null )
        {
            throw new IllegalStateException ( "Unable to provide file information" );
        }

        if ( this.entry != null )
        {
            if ( this.entry.apply ( result ) )
            {
                this.logger.accept ( String.format ( "local override = %s", result ) );
            }
        }

        return result;
    }

    private FileInformation provideByRule ( final Object object, final PayloadEntryType type ) throws IOException
    {
        final FileInformation result = BuilderContext.defaultProvider ().provide ( object, type );

        if ( this.ruleId != null && !this.ruleId.isEmpty () )
        {
            this.logger.accept ( String.format ( "run ruleset: '%s'", this.ruleId ) );
            this.rulesetEval.eval ( this.ruleId, object, type, this.entry.getName (), result );
        }

        this.logger.accept ( String.format ( "fileInformation = %s", result ) );

        return result;
    }
}
